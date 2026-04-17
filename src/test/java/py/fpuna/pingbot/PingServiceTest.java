package py.fpuna.pingbot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import py.fpuna.pingbot.config.PingProperties;
import py.fpuna.pingbot.model.PingResult;
import py.fpuna.pingbot.service.AlertService;
import py.fpuna.pingbot.service.PingService;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PingServiceTest {

    @Mock private WebClient webClient;
    @Mock private WebClient.RequestHeadersUriSpec<?> uriSpec;
    @Mock private WebClient.RequestHeadersSpec<?> headersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;
    @Mock private AlertService alertService;

    private PingProperties props;
    private PingService pingService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        props = new PingProperties();
        props.setTargets(List.of("https://example.com"));
        props.setTimeoutSeconds(5);
        props.setMaxRetries(0);

        pingService = new PingService(webClient, props, alertService);

        when(webClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) uriSpec);
        when(uriSpec.uri(anyString())).thenReturn((WebClient.RequestHeadersSpec) headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void pingAll_successfulResponse_storesSuccessResult() {
        ResponseEntity<Void> okResponse = ResponseEntity.ok().build();
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.just(okResponse));

        pingService.pingAll();

        List<PingResult> results = pingService.getLastResults();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).success()).isTrue();
        assertThat(results.get(0).statusCode()).isEqualTo(200);
        assertThat(results.get(0).url()).isEqualTo("https://example.com");
    }

    @Test
    void pingAll_connectionFailure_storesFailureResult() {
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenReturn(Mono.error(new RuntimeException("Connection refused")));

        pingService.pingAll();

        List<PingResult> results = pingService.getLastResults();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).success()).isFalse();
        // Reactor wraps with "Retries exhausted" but preserves original cause message
        assertThat(results.get(0).errorMessage()).isNotBlank();
    }

    @Test
    void getLastResults_beforeFirstCycle_returnsEmptyList() {
        assertThat(pingService.getLastResults()).isEmpty();
    }
}
