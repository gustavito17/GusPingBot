package py.fpuna.pingbot.model;

import java.time.Instant;
import java.util.List;

public record PingStatusResponse(
        String overallStatus,
        int total,
        int successful,
        int failed,
        List<PingResult> results,
        Instant generatedAt
) {

    public static PingStatusResponse from(List<PingResult> results) {
        if (results.isEmpty()) {
            return new PingStatusResponse("NO_DATA", 0, 0, 0, List.of(), Instant.now());
        }

        long successCount = results.stream().filter(PingResult::success).count();
        long failCount = results.size() - successCount;
        String status = failCount == 0 ? "UP" : (successCount == 0 ? "DOWN" : "DEGRADED");

        return new PingStatusResponse(
                status,
                results.size(),
                (int) successCount,
                (int) failCount,
                results,
                Instant.now()
        );
    }
}
