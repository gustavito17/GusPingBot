package py.fpuna.pingbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pingbot.alert")
public class AlertProperties {

    private String discordWebhookUrl;
    private String slackWebhookUrl;

    /** Consecutive failures before sending an alert (default: 1 = alert on first failure) */
    private int failureThreshold = 1;

    public String getDiscordWebhookUrl() { return discordWebhookUrl; }
    public void setDiscordWebhookUrl(String discordWebhookUrl) { this.discordWebhookUrl = discordWebhookUrl; }

    public String getSlackWebhookUrl() { return slackWebhookUrl; }
    public void setSlackWebhookUrl(String slackWebhookUrl) { this.slackWebhookUrl = slackWebhookUrl; }

    public int getFailureThreshold() { return failureThreshold; }
    public void setFailureThreshold(int failureThreshold) { this.failureThreshold = failureThreshold; }

    public boolean isDiscordEnabled() { return discordWebhookUrl != null && !discordWebhookUrl.isBlank(); }
    public boolean isSlackEnabled()   { return slackWebhookUrl   != null && !slackWebhookUrl.isBlank(); }
}
