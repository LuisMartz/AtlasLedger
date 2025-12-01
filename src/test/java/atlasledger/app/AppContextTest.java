package atlasledger.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import atlasledger.model.Worker;
import atlasledger.repository.OrderRepository;
import atlasledger.repository.ProductRepository;
import atlasledger.repository.ProviderRepository;
import atlasledger.service.AnalyticsService;
import atlasledger.service.AuthService;
import atlasledger.service.DatabaseIntegrityService;
import atlasledger.service.DocumentService;
import atlasledger.service.ReportService;
import atlasledger.service.SimulationService;
import atlasledger.service.SyncService;
import atlasledger.utils.NetworkUtils;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppContextTest {

    @Test
    void gettersExposeInjectedDependencies(@TempDir Path tempDir) {
        AppConfig config = AppConfig.of("https://example.test", Duration.ofSeconds(3));
        ProductRepository products = new ProductRepository();
        ProviderRepository providers = new ProviderRepository();
        OrderRepository orders = new OrderRepository();
        NetworkUtils networkUtils = new NetworkUtils(Duration.ofSeconds(2));
        SyncService syncService = new SyncService(config.getApiBaseUrl(), products, providers, orders, networkUtils);
        ReportService reportService = new ReportService(products, orders);
        DatabaseIntegrityService databaseIntegrityService = new DatabaseIntegrityService();
        AnalyticsService analyticsService = new AnalyticsService();
        Path docs = tempDir.resolve("docs");
        DocumentService documentService = new DocumentService(docs);
        AuthService authService = new AuthService();
        SimulationService simulationService = new SimulationService(products, providers, orders);
        Worker worker = new Worker();

        AppContext context = new AppContext(
            config,
            products,
            providers,
            orders,
            syncService,
            reportService,
            databaseIntegrityService,
            analyticsService,
            documentService,
            authService,
            simulationService,
            worker,
            true,
            docs,
            networkUtils
        );

        try {
            assertSame(config, context.getConfig());
            assertSame(products, context.getProductRepository());
            assertSame(providers, context.getProviderRepository());
            assertSame(orders, context.getOrderRepository());
            assertSame(syncService, context.getSyncService());
            assertSame(reportService, context.getReportService());
            assertSame(databaseIntegrityService, context.getDatabaseIntegrityService());
            assertSame(analyticsService, context.getAnalyticsService());
            assertSame(documentService, context.getDocumentService());
            assertSame(authService, context.getAuthService());
            assertSame(simulationService, context.getSimulationService());
            assertSame(worker, context.getWorker());
            assertTrue(context.isLocalMode());
            assertEquals(docs, context.getDocumentsPath());
            assertSame(networkUtils, context.getNetworkUtils());
        } finally {
            syncService.close();
        }
    }
}
