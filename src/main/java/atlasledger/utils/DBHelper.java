package atlasledger.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for managing the SQLite database connection and schema initialization for the AtlasLedger application.
 * <p>
 * This class handles:
 * <ul>
 *   <li>Setting up the database file location and JDBC URL.</li>
 *   <li>Initializing the database schema, including tables and indexes.</li>
 *   <li>Executing an optional initialization SQL script on first run or when the database path is overridden.</li>
 *   <li>Providing a method to obtain a JDBC {@link Connection} to the database.</li>
 * </ul>
 * <p>
 * The database path can be overridden at runtime, and the schema will be (re)initialized as needed.
 * <p>
 * This class is not intended to be instantiated.
 *
 * <h2>Thread Safety</h2>
 * All public methods are thread-safe.
 *
 * <h2>Usage Example</h2>
 * <pre>
 * try (Connection conn = DBHelper.getConnection()) {
 *     // Use the connection
 * }
 * </pre>
 *
 * @author LuisMartz
 * @since 1.0
 */
public final class DBHelper {

    private static final String DB_NAME = "atlasledger.db";
    private static final Path DEFAULT_DB_PATH = Paths.get(System.getProperty("user.home"), ".atlasledger", DB_NAME);

    private static volatile Path databasePath = DEFAULT_DB_PATH;
    private static volatile String url = buildUrl(databasePath);

    static {
        initialise(true);
    }

    private DBHelper() {
    }

    public static synchronized void overrideDatabasePath(Path newPath, boolean runInitScript) {
        if (newPath == null) {
            return;
        }
        databasePath = newPath;
        url = buildUrl(databasePath);
        Logger.resetPersistence();
        initialise(runInitScript);
    }

    public static Path getDatabasePath() {
        return databasePath;
    }

    private static String buildUrl(Path path) {
        return "jdbc:sqlite:" + path.toAbsolutePath().toString().replace("\\", "/");
    }

    private static void initialise(boolean runInitScript) {
        try {
            Files.createDirectories(databasePath.getParent());
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            Logger.error(DBHelper.class, "Error preparando el driver SQLite", e);
            return;
        }

        try (Connection conn = DriverManager.getConnection(url)) {
            conn.createStatement().execute("PRAGMA foreign_keys = ON;");
            if (runInitScript) {
                executeInitScript(conn);
            }
            createTables(conn);
            createIndexes(conn);
        } catch (SQLException e) {
            Logger.error(DBHelper.class, "Error al inicializar la base de datos", e);
        }
    }

    private static void executeInitScript(Connection conn) {
        Path scriptPath = Paths.get("atlasledger", "resources", "init.sql");
        if (!Files.exists(scriptPath)) {
            return;
        }
        try {
            String script = Files.readString(scriptPath, StandardCharsets.UTF_8);
            for (String rawStatement : script.split(";")) {
                String statement = rawStatement.trim();
                if (statement.isEmpty() || statement.startsWith("--")) {
                    continue;
                }
                try (Statement st = conn.createStatement()) {
                    st.execute(statement);
                }
            }
        } catch (IOException | SQLException e) {
            Logger.warn(DBHelper.class, "No se pudo ejecutar el script init.sql: " + e.getMessage());
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.addBatch("""
                CREATE TABLE IF NOT EXISTS productos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    codigo TEXT UNIQUE NOT NULL,
                    nombre TEXT NOT NULL,
                    categoria TEXT,
                    proveedor_codigo TEXT,
                    stock INTEGER DEFAULT 0,
                    coste REAL DEFAULT 0.0,
                    precio REAL DEFAULT 0.0,
                    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
                );
            """);
            statement.addBatch("""
                CREATE TABLE IF NOT EXISTS proveedores (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    codigo TEXT UNIQUE NOT NULL,
                    nombre TEXT NOT NULL,
                    email TEXT,
                    telefono TEXT,
                    direccion TEXT,
                    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
                );
            """);
            statement.addBatch("""
                CREATE TABLE IF NOT EXISTS ordenes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    codigo TEXT UNIQUE NOT NULL,
                    fecha TEXT,
                    proveedor_codigo TEXT,
                    total REAL DEFAULT 0.0,
                    estado TEXT DEFAULT 'BORRADOR',
                    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (proveedor_codigo) REFERENCES proveedores(codigo)
                );
            """);
            statement.addBatch("""
                CREATE TABLE IF NOT EXISTS informes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT UNIQUE NOT NULL,
                    tipo TEXT NOT NULL,
                    definicion_json TEXT,
                    generado_en TEXT
                );
            """);
            statement.addBatch("""
                CREATE TABLE IF NOT EXISTS sync_queue (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    entidad TEXT NOT NULL,
                    referencia TEXT NOT NULL,
                    payload TEXT NOT NULL,
                    operacion TEXT NOT NULL,
                    estado TEXT DEFAULT 'PENDING',
                    intentos INTEGER DEFAULT 0,
                    ultimo_intento TEXT,
                    creado_en TEXT DEFAULT CURRENT_TIMESTAMP
                );
            """);
            statement.addBatch("""
                CREATE TABLE IF NOT EXISTS app_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    level TEXT NOT NULL,
                    source TEXT NOT NULL,
                    message TEXT NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                );
            """);
            statement.addBatch("""
                CREATE TABLE IF NOT EXISTS workers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    nombre TEXT,
                    rol TEXT,
                    last_login TEXT
                );
            """);
            statement.addBatch("""
                CREATE TABLE IF NOT EXISTS document_queue (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    file_name TEXT NOT NULL,
                    local_path TEXT NOT NULL,
                    status TEXT DEFAULT 'PENDING',
                    uploaded_at TEXT,
                    metadata TEXT
                );
            """);
            statement.executeBatch();
        }
    }

    private static void createIndexes(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.addBatch("CREATE INDEX IF NOT EXISTS idx_productos_codigo ON productos (codigo);");
            statement.addBatch("CREATE INDEX IF NOT EXISTS idx_proveedores_codigo ON proveedores (codigo);");
            statement.addBatch("CREATE INDEX IF NOT EXISTS idx_ordenes_codigo ON ordenes (codigo);");
            statement.addBatch("CREATE INDEX IF NOT EXISTS idx_sync_estado ON sync_queue (estado);");
            statement.addBatch("CREATE INDEX IF NOT EXISTS idx_logs_level ON app_logs (level);");
            statement.addBatch("CREATE INDEX IF NOT EXISTS idx_workers_username ON workers (username);");
            statement.executeBatch();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }
}
