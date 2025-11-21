# AtlasLedger

AtlasLedger is a small Java/JavaFX desktop application for managing products, providers and orders with local SQLite persistence and optional remote synchronization.

## Contents

- **Source**: [atlasledger](atlasledger)
- **UI**: JavaFX components under [ui](ui)
- **DB helpers & utils**: [utils](utils)
- **Services**: [service](service)
- **Repositories / DAOs**: [repository](repository) / [dao](dao)
- **Resources**: 
  - Styles & init SQL: [resources/init.sql](resources/init.sql), [resources/styles/main.css](resources/styles/main.css)

## Requirements

- Java 17+ (OpenJDK or Oracle JDK)
- JavaFX SDK compatible with your JDK (if running outside an IDE)
- SQLite (bundled via JDBC driver; ensure `org.sqlite.JDBC` is available on the classpath)

## Quick Start

### Using an IDE

1. Open the project folder in your IDE (IntelliJ IDEA / VS Code)
2. Add JavaFX libraries to the project run configuration (modules: `javafx.controls`, `javafx.fxml`)
3. Run the main class: [`atlasledger.app.MainApp`](app/MainApp.java)

### From the Command Line

1. **Compile**:
   ```bash
   javac -d out $(find . -name "*.java")
   ```

2. **Run** (adjust path to your JavaFX SDK libs):
   ```bash
   java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -cp out atlasledger.app.MainApp
   ```

## Configuration & Startup

- **Default DB file path**: Managed by `atlasledger.utils.DBHelper`
- **Login screen**: [`atlasledger.ui.login.LoginScreen`](ui/login/LoginScreen.java)
- **App-wide configuration**: Provided by `atlasledger.app.AppConfig`
- **Application initialization**: [`atlasledger.app.AppInitializer`](app/AppInitializer.java)
- **Running context**: Exposed via `atlasledger.app.AppContext`

## Key Components

### Core Classes

- **Entry point**: `atlasledger.app.MainApp`
- **App bootstrap**: `atlasledger.app.AppInitializer`
- **DB helper & schema init**: `atlasledger.utils.DBHelper`
- **Network & JSON stubs**: `atlasledger.utils.NetworkUtils`
- **Logging**: `atlasledger.utils.Logger`
- **Authentication**: `atlasledger.service.AuthService`
- **Sync with remote API**: `atlasledger.service.SyncService`
- **Reporting**: `atlasledger.service.ReportService`
- **Analytics**: `atlasledger.service.AnalyticsService`
- **Password hashing**: `atlasledger.utils.PasswordUtils`

### UI Modules

- **Login**: `atlasledger.ui.login.LoginScreen`
- **Products**: `atlasledger.ui.productos.ProductModule`
- **Providers**: `atlasledger.ui.proveedores.ProviderModule`
- **Reports**: `atlasledger.ui.informes.ReportModule`
- **Analytics / Dashboard**: `atlasledger.ui.dashboard.AnalyticsModule`

### Data Access

- **Repositories**:
  - [`atlasledger.repository.ProductRepository`](repository/ProductRepository.java)
  - [`atlasledger.repository.ProviderRepository`](repository/ProviderRepository.java)
  - [`atlasledger.repository.OrderRepository`](repository/OrderRepository.java)

- **DAOs**:
  - [`dao/ProductoDao.java`](dao/ProductoDao.java)
  - [`dao/ProveedorDao.java`](dao/ProveedorDao.java)
  - [`dao/OrdenDao.java`](dao/OrdenDao.java)

## Database

- **Schema and initial SQL**: [resources/init.sql](resources/init.sql)
- The DB file defaults to a user-local path
- Change the path via the login screen or programmatically through `atlasledger.utils.DBHelper.overrideDatabasePath`

## Development Notes

- The JSON parsing in `atlasledger.utils.NetworkUtils` is a stub — replace with a real JSON library (Jackson/Gson) for production
- Sync uses `atlasledger.service.SyncService` with `parseJsonArray` / mapping helpers in `NetworkUtils` to support remote payloads
- **UI styles**: [resources/styles/main.css](resources/styles/main.css)

## Troubleshooting

- **Missing JavaFX modules**: Ensure `--module-path` points to JavaFX SDK and `--add-modules` includes required modules
- **DB errors**: Inspect logs produced via `atlasledger.utils.Logger` and verify the SQLite JDBC driver is present
- **Remote API connectivity**: If the app cannot reach the remote API, `atlasledger.utils.NetworkUtils.isOnline` performs a basic reachability check

## Contributing

1. Fork the repository
2. Create a feature branch
3. Open a pull request with tests and a short description
4. Keep UI strings in Spanish consistent with existing labels

## License

Add your preferred project license file (e.g. `LICENSE`) to the repository.

---

**Note**: If more specific run scripts or build files (Maven/Gradle) are desired, they can be added to the repository.
