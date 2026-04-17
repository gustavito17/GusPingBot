package py.fpuna.pingbot.model;

import java.time.Instant;

public record PingResult(
        String url,
        int statusCode,
        long responseTimeMs,
        boolean success,
        String errorMessage,
        Instant timestamp
) {

    public static PingResult success(String url, int statusCode, long responseTimeMs) {
        return new PingResult(url, statusCode, responseTimeMs, true, null, Instant.now());
    }

    public static PingResult failure(String url, String errorMessage) {
        return new PingResult(url, 0, -1, false, errorMessage, Instant.now());
    }
}
