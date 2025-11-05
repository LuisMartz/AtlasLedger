package atlasledger.app;

import atlasledger.model.Worker;
import java.nio.file.Path;

public class StartupProfile {

    private final Path databasePath;
    private final Path documentsPath;
    private final boolean localMode;
    private final String apiBaseUrl;
    private final Worker worker;

    public StartupProfile(Path databasePath, Path documentsPath, boolean localMode, String apiBaseUrl, Worker worker) {
        this.databasePath = databasePath;
        this.documentsPath = documentsPath;
        this.localMode = localMode;
        this.apiBaseUrl = apiBaseUrl;
        this.worker = worker;
    }

    public Path getDatabasePath() {
        return databasePath;
    }

    public Path getDocumentsPath() {
        return documentsPath;
    }

    public boolean isLocalMode() {
        return localMode;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public Worker getWorker() {
        return worker;
    }
}
