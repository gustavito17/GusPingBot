package py.fpuna.pingbot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import py.fpuna.pingbot.model.PingResult;
import py.fpuna.pingbot.model.PingStatusResponse;
import py.fpuna.pingbot.service.PingService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/status")
public class PingStatusController {

    private final PingService pingService;

    public PingStatusController(PingService pingService) {
        this.pingService = pingService;
    }

    /**
     * Returns the results of the last ping cycle.
     * GET /api/v1/status
     */
    @GetMapping
    public ResponseEntity<PingStatusResponse> getStatus() {
        List<PingResult> results = pingService.getLastResults();
        return ResponseEntity.ok(PingStatusResponse.from(results));
    }

    /**
     * Triggers an immediate ping cycle outside of the cron schedule.
     * POST /api/v1/status/ping-now
     */
    @GetMapping("/ping-now")
    public ResponseEntity<PingStatusResponse> pingNow() {
        pingService.pingAll();
        return ResponseEntity.ok(PingStatusResponse.from(pingService.getLastResults()));
    }
}
