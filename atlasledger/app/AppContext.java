package atlasledger.app;

import atlasledger.repository.OrderRepository;
import atlasledger.repository.ProductRepository;
import atlasledger.repository.ProviderRepository;
import atlasledger.service.AnalyticsService;
import atlasledger.service.DatabaseIntegrityService;
import atlasledger.service.ReportService;
import atlasledger.service.SyncService;
import atlasledger.utils.NetworkUtils;

public class AppContext {

    private final AppConfig config;
    private final ProductRepository productRepository;
    private final ProviderRepository providerRepository;
    private final OrderRepository orderRepository;
    private final SyncService syncService;
    private final ReportService reportService;
    private final DatabaseIntegrityService databaseIntegrityService;
    private final AnalyticsService analyticsService;
    private final NetworkUtils networkUtils;

    public AppContext(AppConfig config,
                      ProductRepository productRepository,
                      ProviderRepository providerRepository,
                      OrderRepository orderRepository,
                      SyncService syncService,
                      ReportService reportService,
                      DatabaseIntegrityService databaseIntegrityService,
                      AnalyticsService analyticsService,
                      NetworkUtils networkUtils) {
        this.config = config;
        this.productRepository = productRepository;
        this.providerRepository = providerRepository;
        this.orderRepository = orderRepository;
        this.syncService = syncService;
        this.reportService = reportService;
        this.databaseIntegrityService = databaseIntegrityService;
        this.analyticsService = analyticsService;
        this.networkUtils = networkUtils;
    }

    public AppConfig getConfig() {
        return config;
    }

    public ProductRepository getProductRepository() {
        return productRepository;
    }

    public ProviderRepository getProviderRepository() {
        return providerRepository;
    }

    public OrderRepository getOrderRepository() {
        return orderRepository;
    }

    public SyncService getSyncService() {
        return syncService;
    }

    public ReportService getReportService() {
        return reportService;
    }

    public DatabaseIntegrityService getDatabaseIntegrityService() {
        return databaseIntegrityService;
    }

    public AnalyticsService getAnalyticsService() {
        return analyticsService;
    }

    public NetworkUtils getNetworkUtils() {
        return networkUtils;
    }
}
