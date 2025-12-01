package atlasledger.app;

import java.time.Duration;

/**
 * Configuration class for application settings such as API base URL and network timeout.
 * <p>
 * This class provides immutable configuration values required for connecting to external services.
 * It includes factory methods for loading default settings or creating custom configurations.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     AppConfig config = AppConfig.loadDefault();
 *     String baseUrl = config.getApiBaseUrl();
 *     Duration timeout = config.getNetworkTimeout();
 * </pre>
 *
 * @author LuisMartz
 */
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
