package atlasledger.app;

import java.time.Duration;

public class AppConfig {

    private final String apiBaseUrl;
    private final Duration networkTimeout;

    public AppConfig(String apiBaseUrl, Duration networkTimeout) {
        this.apiBaseUrl = apiBaseUrl;
        this.networkTimeout = networkTimeout;
    }

    public static AppConfig loadDefault() {
        return new AppConfig("https://api.atlas-ledger.example", Duration.ofSeconds(10));
    }

    public static AppConfig of(String apiBaseUrl, Duration networkTimeout) {
        return new AppConfig(apiBaseUrl, networkTimeout);
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public Duration getNetworkTimeout() {
        return networkTimeout;
    }
}
