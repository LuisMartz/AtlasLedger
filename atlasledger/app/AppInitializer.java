package atlasledger.app;

import atlasledger.repository.OrderRepository;
import atlasledger.repository.ProductRepository;
import atlasledger.repository.ProviderRepository;
import atlasledger.service.AnalyticsService;
import atlasledger.service.DatabaseIntegrityService;
import atlasledger.service.ReportService;
import atlasledger.service.SyncService;
import atlasledger.utils.NetworkUtils;

public final class AppInitializer {

    private AppInitializer() {
    }

    public static AppContext initialise() {
        AppConfig config = AppConfig.loadDefault();
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

        return new AppContext(
            config,
            productRepository,
            providerRepository,
            orderRepository,
            syncService,
            reportService,
            integrityService,
            analyticsService,
            networkUtils
        );
    }
}
