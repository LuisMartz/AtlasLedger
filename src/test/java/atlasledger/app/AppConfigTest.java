package atlasledger.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class AppConfigTest {

    @Test
    void loadDefaultProvidesExpectedValues() {
        AppConfig config = AppConfig.loadDefault();

        assertEquals("https://api.atlas-ledger.example", config.getApiBaseUrl());
        assertEquals(Duration.ofSeconds(10), config.getNetworkTimeout());
    }

    @Test
    void customFactoryCreatesConfig() {
        Duration timeout = Duration.ofSeconds(5);
        AppConfig config = AppConfig.of("https://example.test", timeout);

        assertEquals("https://example.test", config.getApiBaseUrl());
        assertEquals(timeout, config.getNetworkTimeout());
    }
}
