package atlasledger.app;

import atlasledger.model.Worker;
import atlasledger.repository.OrderRepository;
import atlasledger.repository.ProductRepository;
import atlasledger.repository.ProviderRepository;
import atlasledger.service.AnalyticsService;
import atlasledger.service.AuthService;
import atlasledger.service.DatabaseIntegrityService;
import atlasledger.service.DocumentService;
import atlasledger.service.ReportService;
import atlasledger.service.SyncService;
import atlasledger.utils.NetworkUtils;
import java.nio.file.Path;

/**
 * The {@code AppContext} class serves as a centralized container for application-wide
 * configuration and service dependencies. It encapsulates references to various repositories,
 * services, configuration objects, and utility classes required throughout the application.
 * <p>
 * This class is typically instantiated at application startup and passed to components
 * that require access to shared resources or services.
 * </p>
 *
 * <p>
 * The dependencies managed by {@code AppContext} include:
 * <ul>
 *   <li>{@link AppConfig} - Application configuration settings</li>
 *   <li>{@link ProductRepository} - Repository for product data access</li>
 *   <li>{@link ProviderRepository} - Repository for provider data access</li>
 *   <li>{@link OrderRepository} - Repository for order data access</li>
 *   <li>{@link SyncService} - Service for data synchronization</li>
 *   <li>{@link ReportService} - Service for generating reports</li>
 *   <li>{@link DatabaseIntegrityService} - Service for database integrity checks</li>
 *   <li>{@link AnalyticsService} - Service for analytics and metrics</li>
 *   <li>{@link DocumentService} - Service for document management</li>
 *   <li>{@link AuthService} - Service for authentication and authorization</li>
 *   <li>{@link Worker} - Background worker for asynchronous tasks</li>
 *   <li>{@code boolean localMode} - Flag indicating if the application is running in local mode</li>
 *   <li>{@link java.nio.file.Path} documentsPath - Path to the documents directory</li>
 *   <li>{@link NetworkUtils} - Utility class for network operations</li>
 * </ul>
 * </p>
 *
 * <p>
 * All dependencies are injected via the constructor and exposed through getter methods.
 * </p>
 */
public class AppContext {

    private final AppConfig config;
    private final ProductRepository productRepository;
    private final ProviderRepository providerRepository;
    private final OrderRepository orderRepository;
    private final SyncService syncService;
    private final ReportService reportService;
    private final DatabaseIntegrityService databaseIntegrityService;
    private final AnalyticsService analyticsService;
    private final DocumentService documentService;
    private final AuthService authService;
    private final Worker worker;
    private final boolean localMode;
    private final Path documentsPath;
    private final NetworkUtils networkUtils;

    public AppContext(AppConfig config,
                      ProductRepository productRepository,
                      ProviderRepository providerRepository,
                      OrderRepository orderRepository,
                      SyncService syncService,
                      ReportService reportService,
                      DatabaseIntegrityService databaseIntegrityService,
                      AnalyticsService analyticsService,
                      DocumentService documentService,
                      AuthService authService,
                      Worker worker,
                      boolean localMode,
                      Path documentsPath,
                      NetworkUtils networkUtils) {
        this.config = config;
        this.productRepository = productRepository;
        this.providerRepository = providerRepository;
        this.orderRepository = orderRepository;
        this.syncService = syncService;
        this.reportService = reportService;
        this.databaseIntegrityService = databaseIntegrityService;
        this.analyticsService = analyticsService;
        this.documentService = documentService;
        this.authService = authService;
        this.worker = worker;
        this.localMode = localMode;
        this.documentsPath = documentsPath;
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

    public DocumentService getDocumentService() {
        return documentService;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public Worker getWorker() {
        return worker;
    }

    public boolean isLocalMode() {
        return localMode;
    }

    public Path getDocumentsPath() {
        return documentsPath;
    }

    public NetworkUtils getNetworkUtils() {
        return networkUtils;
    }
}
