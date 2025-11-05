package atlasledger.app;

import java.time.Duration;

public class AppConfig {

    private final String apiBaseUrl;
    private final Duration networkTimeout;

    private AppConfig(String apiBaseUrl, Duration networkTimeout) {
        this.apiBaseUrl = apiBaseUrl;
        this.networkTimeout = networkTimeout;
    }

    public static AppConfig loadDefault() {
        return new AppConfig("https://api.atlas-ledger.example", Duration.ofSeconds(10));
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public Duration getNetworkTimeout() {
        return networkTimeout;
    }
}
