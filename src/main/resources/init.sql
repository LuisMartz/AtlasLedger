
-- ===========================================================
-- AtlasLedger - Script inicial de base de datos SQLite
-- Version: 1.1
-- Fecha: 2025-11-05
-- ===========================================================

PRAGMA foreign_keys = ON;

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

CREATE TABLE IF NOT EXISTS proveedores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo TEXT UNIQUE NOT NULL,
    nombre TEXT NOT NULL,
    email TEXT,
    telefono TEXT,
    direccion TEXT,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
);

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

CREATE TABLE IF NOT EXISTS informes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT UNIQUE NOT NULL,
    tipo TEXT NOT NULL,
    definicion_json TEXT,
    generado_en TEXT
);

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

CREATE TABLE IF NOT EXISTS app_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    level TEXT NOT NULL,
    source TEXT NOT NULL,
    message TEXT NOT NULL,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    nombre TEXT,
    rol TEXT,
    last_login TEXT
);

CREATE TABLE IF NOT EXISTS document_queue (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    file_name TEXT NOT NULL,
    local_path TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING',
    uploaded_at TEXT,
    metadata TEXT
);

CREATE INDEX IF NOT EXISTS idx_productos_codigo ON productos (codigo);
CREATE INDEX IF NOT EXISTS idx_proveedores_codigo ON proveedores (codigo);
CREATE INDEX IF NOT EXISTS idx_ordenes_codigo ON ordenes (codigo);
CREATE INDEX IF NOT EXISTS idx_sync_estado ON sync_queue (estado);
CREATE INDEX IF NOT EXISTS idx_logs_level ON app_logs (level);
CREATE INDEX IF NOT EXISTS idx_workers_username ON workers (username);

INSERT INTO proveedores (codigo, nombre, email, telefono, direccion)
VALUES ('GEN001', 'Proveedor Generico', 'contacto@proveedor.com', '000-000-000', 'Sin direccion')
ON CONFLICT DO NOTHING;

INSERT INTO workers (username, password_hash, nombre, rol)
VALUES ('admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'Administrador', 'ADMIN')
ON CONFLICT(username) DO NOTHING;

