package py.fpuna.pingbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "pingbot")
@Validated
public class PingProperties {

    @NotEmpty(message = "At least one target URL must be configured")
    private List<String> targets;

    /** Timeout in seconds for each HTTP request */
    @Min(1)
    private int timeoutSeconds = 10;

    /** Max retries on transient failures per URL per cycle */
    @Min(0)
    private int maxRetries = 2;

    public List<String> getTargets() {
        return targets;
    }

    public void setTargets(List<String> targets) {
        this.targets = targets;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
}
