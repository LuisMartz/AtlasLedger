
package atlasledger.app;

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
import atlasledger.utils.DBHelper;
import atlasledger.utils.NetworkUtils;
import java.nio.file.Path;
import java.time.Duration;

public final class AppInitializer {

    private AppInitializer() {
    }

    public static AppContext initialise(StartupProfile profile, AuthService authService) {
        Path databasePath = profile.getDatabasePath();
        DBHelper.overrideDatabasePath(databasePath, true);
        authService.ensureDefaultAdmin();

        AppConfig config = AppConfig.of(profile.getApiBaseUrl(), Duration.ofSeconds(10));
        NetworkUtils networkUtils = new NetworkUtils(config.getNetworkTimeout());

        ProductRepository productRepository = new ProductRepository();
        ProviderRepository providerRepository = new ProviderRepository();
        OrderRepository orderRepository = new OrderRepository();

        SyncService syncService = new SyncService(
            config.getApiBaseUrl(),
            productRepository,
            providerRepository,
            orderRepository,
            networkUtils
        );

        ReportService reportService = new ReportService(productRepository, orderRepository);
        DatabaseIntegrityService integrityService = new DatabaseIntegrityService();
        AnalyticsService analyticsService = new AnalyticsService();
        DocumentService documentService = new DocumentService(profile.getDocumentsPath());
        SimulationService simulationService = new SimulationService(productRepository, providerRepository, orderRepository);

        return new AppContext(
            config,
            productRepository,
            providerRepository,
            orderRepository,
            syncService,
            reportService,
            integrityService,
            analyticsService,
            documentService,
            authService,
            simulationService,
            profile.getWorker(),
            profile.isLocalMode(),
            profile.getDocumentsPath(),
            networkUtils
        );
    }
}

