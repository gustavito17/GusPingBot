package py.fpuna.pingbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import py.fpuna.pingbot.config.AlertProperties;
import py.fpuna.pingbot.model.PingResult;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault());

    private final WebClient webClient;
    private final AlertProperties alertProps;

    /** Tracks consecutive failure count per URL to enforce failureThreshold */
    private final Map<String, Integer> failureCount = new ConcurrentHashMap<>();

    public AlertService(WebClient pingWebClient, AlertProperties alertProps) {
        this.webClient = pingWebClient;
        this.alertProps = alertProps;
    }

    /**
     * Called after each ping cycle.
     * Sends alerts for failed URLs that crossed the threshold,
     * and resets the counter for URLs that recovered.
     */
    public void processResults(List<PingResult> results) {
        if (!alertProps.isDiscordEnabled() && !alertProps.isSlackEnabled()) {
            return;
        }

        for (PingResult result : results) {
            if (result.success()) {
                handleRecovery(result);
            } else {
                handleFailure(result);
            }
        }
    }

    private void handleFailure(PingResult result) {
        int count = failureCount.merge(result.url(), 1, Integer::sum);
        if (count == alertProps.getFailureThreshold()) {
            String message = buildFailureMessage(result);
            sendAlerts(message);
        }
    }

    private void handleRecovery(PingResult result) {
        Integer previous = failureCount.remove(result.url());
        // Only notify recovery if we had previously sent a failure alert
        if (previous != null && previous >= alertProps.getFailureThreshold()) {
            String message = buildRecoveryMessage(result);
            sendAlerts(message);
        }
    }

    private void sendAlerts(String message) {
        if (alertProps.isDiscordEnabled()) {
            sendDiscord(message);
        }
        if (alertProps.isSlackEnabled()) {
            sendSlack(message);
        }
    }

    private void sendDiscord(String message) {
        // Discord webhook expects: {"content": "..."}
        String body = "{\"content\": " + jsonString(message) + "}";

        webClient.post()
                .uri(alertProps.getDiscordWebhookUrl())
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(r -> log.info("Discord alert sent (HTTP {})", r.getStatusCode()))
                .doOnError(e -> log.error("Failed to send Discord alert: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }

    private void sendSlack(String message) {
        // Slack incoming webhook expects: {"text": "..."}
        String body = "{\"text\": " + jsonString(message) + "}";

        webClient.post()
                .uri(alertProps.getSlackWebhookUrl())
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(r -> log.info("Slack alert sent (HTTP {})", r.getStatusCode()))
                .doOnError(e -> log.error("Failed to send Slack alert: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }

    private String buildFailureMessage(PingResult r) {
        return """
                🔴 PING FAILURE — Always-On Ping Bot
                URL: %s
                Error: %s
                Time: %s
                """.formatted(r.url(), r.errorMessage(), FMT.format(r.timestamp()));
    }

    private String buildRecoveryMessage(PingResult r) {
        return """
                ✅ SERVICE RECOVERED — Always-On Ping Bot
                URL: %s
                Status: HTTP %d in %d ms
                Time: %s
                """.formatted(r.url(), r.statusCode(), r.responseTimeMs(), FMT.format(r.timestamp()));
    }

    /** Escapes a string as a JSON string literal */
    private String jsonString(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r") + "\"";
    }
}
