package py.fpuna.pingbot.service;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import py.fpuna.pingbot.model.PingResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("pingTargets")
public class PingHealthIndicator implements HealthIndicator {

    private final PingService pingService;

    public PingHealthIndicator(PingService pingService) {
        this.pingService = pingService;
    }

    @Override
    public Health health() {
        List<PingResult> results = pingService.getLastResults();

        if (results.isEmpty()) {
            return Health.unknown().withDetail("info", "No ping cycle has run yet").build();
        }

        Map<String, Object> details = results.stream()
                .collect(Collectors.toMap(
                        PingResult::url,
                        r -> r.success()
                                ? "OK (%d ms, HTTP %d)".formatted(r.responseTimeMs(), r.statusCode())
                                : "FAIL: " + r.errorMessage()));

        boolean allUp = results.stream().allMatch(PingResult::success);
        return allUp ? Health.up().withDetails(details).build()
                     : Health.down().withDetails(details).build();
    }
}
