package atlasledger.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DBHelper {

    private static final String DB_NAME = "atlasledger.db";
    private static final String URL = "jdbc:sqlite:" + DB_NAME;

    static {
        initialise();
    }

    private DBHelper() {
    }

    private static void initialise() {
        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.createStatement().execute("PRAGMA foreign_keys = ON;");
            createTables(conn);
        } catch (SQLException e) {
            Logger.error(DBHelper.class, "Error al inicializar la base de datos", e);
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
            statement.executeBatch();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
