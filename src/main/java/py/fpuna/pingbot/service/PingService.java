package py.fpuna.pingbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import py.fpuna.pingbot.config.PingProperties;
import py.fpuna.pingbot.model.PingResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class PingService {

    private static final Logger log = LoggerFactory.getLogger(PingService.class);

    private final WebClient webClient;
    private final PingProperties props;
    private final AlertService alertService;

    /** In-memory ring of the last results — exposed to Actuator health indicator */
    private final CopyOnWriteArrayList<PingResult> lastResults = new CopyOnWriteArrayList<>();

    public PingService(WebClient pingWebClient, PingProperties props, AlertService alertService) {
        this.webClient = pingWebClient;
        this.props = props;
        this.alertService = alertService;
    }

    /**
     * Fires all configured pings in parallel.
     * Schedule is read from application.properties: pingbot.cron (default: every 10 min).
     */
    @Scheduled(cron = "${pingbot.cron:0 */10 * * * *}")
    public void pingAll() {
        log.info(">>> Starting ping cycle for {} target(s)", props.getTargets().size());

        List<PingResult> results = Flux.fromIterable(props.getTargets())
                .flatMap(this::pingWithRetry)
                .collectList()
                .block(Duration.ofSeconds((long) props.getTimeoutSeconds() * (props.getMaxRetries() + 2)));

        if (results != null) {
            lastResults.clear();
            lastResults.addAll(results);
            results.forEach(this::logResult);
            alertService.processResults(results);
        }

        log.info("<<< Ping cycle completed");
    }

    private Mono<PingResult> pingWithRetry(String url) {
        long startTime = System.currentTimeMillis();

        return webClient.get()
                .uri(url)
                .retrieve()
                // Treat 4xx/5xx as errors so the retry logic kicks in
                .onStatus(HttpStatus::isError, response ->
                        Mono.error(new WebClientResponseException(
                                response.statusCode().value(),
                                "HTTP error from " + url,
                                null, null, null)))
                .toBodilessEntity()
                .map(response -> PingResult.success(
                        url,
                        response.getStatusCode().value(),
                        System.currentTimeMillis() - startTime))
                .retryWhen(Retry.backoff(props.getMaxRetries(), Duration.ofSeconds(2))
                        .filter(this::isTransient)
                        .doBeforeRetry(signal ->
                                log.warn("Retrying {} (attempt {}): {}",
                                        url, signal.totalRetries() + 1, signal.failure().getMessage())))
                .onErrorResume(ex -> {
                    log.error("FAILED ping to {}: {}", url, ex.getMessage());
                    return Mono.just(PingResult.failure(url, ex.getMessage()));
                });
    }

    /** Only retry on connection/timeout errors, not on 4xx client errors */
    private boolean isTransient(Throwable t) {
        return !(t instanceof WebClientResponseException wcre
                && wcre.getStatusCode().is4xxClientError());
    }

    private void logResult(PingResult r) {
        if (r.success()) {
            log.info("  [OK]  {} → HTTP {} in {}ms", r.url(), r.statusCode(), r.responseTimeMs());
        } else {
            log.error("  [ERR] {} → {}", r.url(), r.errorMessage());
        }
    }

    public List<PingResult> getLastResults() {
        return List.copyOf(lastResults);
    }
}
