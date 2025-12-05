# AtlasLedger

**Una aplicación desktop Java/JavaFX para gestionar productos, proveedores y órdenes con persistencia local SQLite y sincronización remota**.

![Java](https://img.shields.io/badge/Java-11%2B-orange)
![Maven](https://img.shields.io/badge/Maven-3.8%2B-blue)
![JavaFX](https://img.shields.io/badge/JavaFX-17-brightgreen)
![SQLite](https://img.shields.io/badge/SQLite-3.44-lightblue)
![License](https://img.shields.io/badge/License-MIT-green)

---

## 📋 Tabla de Contenidos

- [Screenshots](#screenshots)
- [Tutorial Rápido](#tutorial-rápido)
- [Flujo de Uso](#flujo-de-uso)
- [Requisitos](#requisitos)
- [Instalación](#instalación)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Arquitectura](#arquitectura)
- [Build y Compilación](#build-y-compilación)
- [Tests](#tests)
- [Estado de Sincronización](#estado-de-sincronización)
- [Troubleshooting](#troubleshooting)
- [Contribuir](#contribuir)
- [Licencia](#licencia)

---

## 📸 Screenshots

> - Pantalla de Login
> - Dashboard Principal
> - Módulo de Productos
> - Módulo de Proveedores
> - Módulo de Órdenes
> - Reportes e Informes


---

## 🚀 Tutorial Rápido

### 1. **Clonar el repositorio**
```bash
git clone https://github.com/LuisMartz/AtlasLedger.git
cd AtlasLedger
```

### 2. **Verificar requisitos**
```bash
java -version          # Java 11 o superior
mvn -version           # Maven 3.8 o superior
```

### 3. **Compilar el proyecto**
```bash
mvn clean compile
```

### 4. **Ejecutar tests**
```bash
mvn test
```

### 5. **Generar JAR ejecutable**
```bash
mvn clean package
```

### 6. **Ejecutar la aplicación**

**Opción A: Desde Maven (directamente)**
```bash
mvn javafx:run
```

**Opción B: Desde JAR compilado**
```bash
java -jar target/atlasledger-1.0.0.jar
```

---

## 🔄 Flujo de Uso

### Flujo General del Usuario

```
┌─────────────────┐
│  Iniciar App    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Pantalla Login  │ ◄─── Seleccionar archivo DB (o crear uno nuevo)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Dashboard       │ ◄─── Vista principal con módulos
└────────┬────────┘
         │
    ┌────┴────┬────────┬─────────┬─────────┐
    │          │        │         │         │
    ▼          ▼        ▼         ▼         ▼
 Productos  Órdenes Proveedores Informes Analytics
```

### Módulos Principales

1. **Productos**
   - Crear, editar, eliminar productos
   - Ver inventario
   - Gestionar precios

2. **Proveedores**
   - Registrar proveedores
   - Ver contacto y ubicación
   - Gestionar relaciones comerciales

3. **Órdenes**
   - Crear órdenes de compra/venta
   - Asignar productos a órdenes
   - Hacer seguimiento de estado

4. **Informes**
   - Generar reportes de inventario
   - Estadísticas de ventas
   - Análisis de proveedores

5. **Dashboard / Analytics**
   - Vista general de KPIs
   - Gráficos de tendencias
   - Alertas de bajo stock

6. **Módulos Especializados**
   - **Trading:** Gestión de operaciones comerciales
   - **Packaging:** Control de empaque y logística
   - **Transport:** Seguimiento de transporte

---

## ✅ Requisitos

### Requisitos del Sistema
- **Java:** 11 o superior (probado en JDK 11, 17, 21)
- **Maven:** 3.8 o superior
- **Sistema Operativo:** Windows, macOS, Linux

### Dependencias Maven (automáticamente instaladas)

| Dependencia | Versión | Propósito |
|---|---|---|
| `org.openjfx:javafx-controls` | 17.0.6 | Framework UI |
| `org.openjfx:javafx-fxml` | 17.0.6 | Markup de UI |
| `org.openjfx:javafx-graphics` | 17.0.6 | Renderizado gráfico |
| `org.xerial:sqlite-jdbc` | 3.44.0.0 | Driver SQLite |
| `org.junit.jupiter:junit-jupiter-api` | 5.9.3 | Testing (JUnit 5) |
| `org.mockito:mockito-core` | 5.3.1 | Mocking en tests |
| `org.slf4j:slf4j-api` | 2.0.9 | Logging |
| `ch.qos.logback:logback-classic` | 1.4.11 | Implementación Logging |

---

## 💻 Instalación

### Instalación Local para Desarrollo

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/LuisMartz/AtlasLedger.git
   cd AtlasLedger
   ```

2. **Abrir en IDE**
   - **IntelliJ IDEA:** `File → Open` → seleccionar carpeta del proyecto
   - **VS Code:** `File → Open Folder` → instalar extensión de Java (Extension Pack for Java)
   - **Eclipse:** `File → Import → Existing Maven Projects`

3. **Maven descargará automáticamente las dependencias**
   ```bash
   mvn dependency:resolve
   ```

4. **Compilar**
   ```bash
   mvn clean compile
   ```

---

## 🏗️ Estructura del Proyecto

```
AtlasLedger/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── atlasledger/
│   │   │       ├── app/                 # Inicialización y configuración
│   │   │       │   ├── MainApp.java
│   │   │       │   ├── AppConfig.java
│   │   │       │   ├── AppContext.java
│   │   │       │   ├── AppInitializer.java
│   │   │       │   └── StartupProfile.java
│   │   │       ├── model/               # Entidades/POJO
│   │   │       │   ├── Producto.java
│   │   │       │   ├── Proveedor.java
│   │   │       │   ├── Orden.java
│   │   │       │   ├── Worker.java
│   │   │       │   ├── AppLog.java
│   │   │       │   ├── Informe.java
│   │   │       │   └── DocumentTask.java
│   │   │       ├── dao/                 # Data Access Objects
│   │   │       │   ├── ProductoDao.java
│   │   │       │   ├── ProveedorDao.java
│   │   │       │   ├── OrdenDao.java
│   │   │       │   ├── WorkerDao.java
│   │   │       │   └── DocumentQueueDao.java
│   │   │       ├── repository/          # Repositories (abstracción sobre DAOs)
│   │   │       │   ├── ProductRepository.java
│   │   │       │   ├── ProviderRepository.java
│   │   │       │   └── OrderRepository.java
│   │   │       ├── service/             # Lógica de negocio
│   │   │       │   ├── AuthService.java
│   │   │       │   ├── DocumentService.java
│   │   │       │   ├── ReportService.java
│   │   │       │   ├── AnalyticsService.java
│   │   │       │   ├── DatabaseIntegrityService.java
│   │   │       │   ├── SimulationService.java
│   │   │       │   ├── SyncService.java
│   │   │       │   └── ReportSnapshot.java
│   │   │       ├── simulation/          # Simulación de eventos
│   │   │       │   ├── PackagingEvent.java
│   │   │       │   ├── TransportEvent.java
│   │   │       │   ├── TransportMode.java
│   │   │       │   └── PricePoint.java
│   │   │       ├── ui/                  # Interfaz de Usuario (JavaFX)
│   │   │       │   ├── login/
│   │   │       │   │   └── LoginScreen.java
│   │   │       │   ├── dashboard/
│   │   │       │   │   ├── MainScreen.java
│   │   │       │   │   └── AnalyticsModule.java
│   │   │       │   ├── productos/
│   │   │       │   │   ├── ProductModule.java
│   │   │       │   │   └── ProductDialog.java
│   │   │       │   ├── proveedores/
│   │   │       │   │   ├── ProviderModule.java
│   │   │       │   │   └── ProviderDialog.java
│   │   │       │   ├── ordenes/
│   │   │       │   │   ├── OrderModule.java
│   │   │       │   │   └── OrderDialog.java
│   │   │       │   ├── informes/
│   │   │       │   │   └── ReportModule.java
│   │   │       │   ├── trading/
│   │   │       │   │   └── TradingModule.java
│   │   │       │   ├── packaging/
│   │   │       │   │   └── PackagingModule.java
│   │   │       │   └── transport/
│   │   │       │       └── TransportModule.java
│   │   │       └── utils/               # Utilidades
│   │   │           ├── DBHelper.java
│   │   │           ├── Logger.java
│   │   │           ├── NetworkUtils.java
│   │   │           └── PasswordUtils.java
│   │   ├── resources/
│   │   │   ├── init.sql
│   │   │   └── styles/
│   │   │       └── main.css
│   │   └── module-info.java            # Declaración de módulos
│   ├── test/
│   │   └── java/
│   │       └── atlasledger/
│   │           ├── dao/
│   │           │   ├── ProductoDaoTest.java
│   │           │   └── ProveedorDaoTest.java
│   │           ├── repository/
│   │           │   ├── ProductRepositoryTest.java
│   │           │   └── ProviderRepositoryTest.java
│   │           └── utils/
│   │               ├── DBHelperTest.java
│   │               └── PasswordUtilsTest.java
├── pom.xml                             # Configuración Maven
├── .gitignore                          # Git ignore rules
├── LICENSE                             # Licencia (MIT)
├── README.md                           # Este archivo
└── sources.txt                         # Metadatos del proyecto
```

---

## 🏛️ Arquitectura

### Capas Arquitectónicas

```
┌──────────────────────────────────────────────┐
│           UI Layer (JavaFX)                   │
│   - MainScreen, LoginScreen, Modules         │
│   - Componentes interactivos                 │
└──────────────┬───────────────────────────────┘
               │
┌──────────────▼───────────────────────────────┐
│         Service Layer                         │
│   - AuthService, ReportService               │
│   - AnalyticsService, SyncService            │
│   - DatabaseIntegrityService                 │
└──────────────┬───────────────────────────────┘
               │
┌──────────────▼───────────────────────────────┐
│      Repository/DAO Layer                    │
│   - ProductRepository, ProviderRepository    │
│   - OrderRepository, ProductoDao, etc.       │
└──────────────┬───────────────────────────────┘
               │
┌──────────────▼───────────────────────────────┐
│       Data Access Layer                      │
│   - SQLite JDBC Driver                       │
│   - DBHelper (pool de conexiones)            │
└──────────────┬───────────────────────────────┘
               │
┌──────────────▼───────────────────────────────┐
│         SQLite Database                      │
│   - Tablas: productos, proveedores,          │
│             órdenes, workers, logs           │
└──────────────────────────────────────────────┘
```

### Patrones de Diseño

- **DAO (Data Access Object):** Abstracción de acceso a datos en `dao/`
- **Repository:** Capa adicional de abstracción en `repository/`
- **Service:** Lógica de negocio centralizada en `service/`
- **Singleton:** `AppContext`, `Logger`, `DBHelper`
- **Factory/Builder:** Inicialización en `AppInitializer`
- **MVC (Model-View-Controller):** Separación en UI, Service, Model

### Flujo de Datos

```
Usuario interactúa con UI
       ↓
Module/Screen (UI)
       ↓
Service Layer (lógica de negocio)
       ↓
Repository/DAO (persistencia)
       ↓
SQLite Database
```

---

## 🔨 Build y Compilación

### Tareas Maven Disponibles

```bash
# Compilar código
mvn clean compile

# Ejecutar tests
mvn test

# Ejecutar tests específicos
mvn test -Dtest=ProductoDaoTest

# Generar JAR ejecutable (fat JAR con todas las dependencias)
mvn clean package

# Limpiar builds anteriores
mvn clean

# Ejecutar aplicación desde Maven
mvn javafx:run

# Ver árbol de dependencias
mvn dependency:tree

# Ver resumen de proyecto
mvn project-info-reports:project-info
```

### Generar JAR Ejecutable

```bash
mvn clean package
```

**Output:** `target/atlasledger-1.0.0.jar`

**Ejecutar:**
```bash
java -jar target/atlasledger-1.0.0.jar
```

### Compilación Manual (sin Maven)

```bash
# Compilar
javac -d out $(find src/main/java -name "*.java")

# Ejecutar (requiere JavaFX SDK)
java --module-path /path/to/javafx/lib \
     --add-modules javafx.controls,javafx.fxml \
     -cp out:libs/* atlasledger.app.MainApp
```

---

## 🧪 Tests

### Ejecutar Todos los Tests

```bash
mvn test
```

### Ejecutar Tests Específicos

```bash
mvn test -Dtest=ProductoDaoTest
mvn test -Dtest=*RepositoryTest
```

### Cobertura de Tests

Los tests están organizados en `src/test/java/atlasledger/`:

- **`dao/`** - Tests de DAOs (acceso a datos)
- **`repository/`** - Tests de Repositories
- **`utils/`** - Tests de utilidades

### Ejemplos de Archivos de Test

#### `ProductoDaoTest.java`
```java
class ProductoDaoTest {
    private ProductoDao productoDao;
    private MockedConnection mockConnection;

    @BeforeEach
    void setUp() {
        mockConnection = mock(Connection.class);
        productoDao = new ProductoDao(mockConnection);
    }

    @Test
    void testCrearProducto() {
        // Arrange
        Producto producto = new Producto("Test", 100.0, 50);
        
        // Act
        productoDao.crear(producto);
        
        // Assert
        assertTrue(productoDao.obtenerTodos().contains(producto));
    }
}
```

### Notas sobre Tests

- **Especulativo:** Los tests actualmente son plantillas; deben ejecutarse y ajustarse según la implementación real.
- **Dependencias de BD:** Los DAOs requieren mocks de conexión; ajusta según tus necesidades.
- **Instrucciones para validar localmente:**
  1. Asegúrate de que SQLite JDBC esté en classpath
  2. Ejecuta `mvn clean test` en la terminal
  3. Revisa resultados en `target/surefire-reports/`

---

## 🔄 Estado de Sincronización

### Sincronización Remota: STUB (Plan Futuro)

**Estado Actual:** La sincronización remota es un **stub/placeholder** y NO está completamente implementada.

**Componentes Relacionados:**
- `SyncService.java` - Servicio de sincronización
- `NetworkUtils.java` - Utilidades de red (parseJson es básico)

**Características Planeadas:**
- [ ] Sincronización bidirecional con servidor remoto
- [ ] Manejo de conflictos de datos
- [ ] Queue de cambios offline
- [ ] Validación de integridad post-sincronización

**Cómo Implementar:**
1. Reemplazar el parseo JSON en `NetworkUtils` con Jackson o Gson
2. Implementar endpoints REST en `SyncService`
3. Agregar estrategia de resolución de conflictos
4. Crear tests de integración

---

## 🐛 Troubleshooting

### 1. Error: `javafx: not found`
**Solución:**
```bash
# Instalar plugin de JavaFX
mvn dependency:resolve

# O ejecutar directamente:
mvn javafx:run
```

### 2. Error: `SQLite JDBC Driver not found`
**Solución:**
```bash
# Actualizar dependencias
mvn clean dependency:resolve

# Verificar classpath
mvn dependency:tree | grep sqlite
```

### 3. Error: `Module not found: javafx.controls`
**Solución (IDE):**
- Agregar `--module-path` a JVM options
- Configurar en `pom.xml` (ya incluido)

### 4. Base de datos corrupta o no existe
**Solución:**
1. Eliminar archivo `.db` (generalmente en `~/.atlasledger/`)
2. Reiniciar aplicación
3. Base de datos se recreará automáticamente desde `init.sql`

### 5. Error de Permisos al acceder a BD
**Solución (Windows):**
```powershell
# Ejecutar como administrador
java -jar target/atlasledger-1.0.0.jar
```

### 6. Aplicación lenta o freezes
**Solución:**
1. Verificar logs: `Logger.java`
2. Aumentar heap memory: `java -Xmx2G -jar atlasledger-1.0.0.jar`
3. Revisar queries de BD en `DBHelper.java`

---

## 🤝 Contribuir

1. **Fork** el repositorio
2. **Crea una rama** para tu feature:
   ```bash
   git checkout -b feature/tu-feature
   ```
3. **Commituea tus cambios:**
   ```bash
   git commit -m "Agregar tu-feature"
   ```
4. **Push a la rama:**
   ```bash
   git push origin feature/tu-feature
   ```
5. **Abre un Pull Request** con descripción clara

### Estándares de Contribución
- Java 11+ syntax
- Seguir convenciones de nombres existentes
- Agregar tests para nuevas funcionalidades
- Documentar cambios en README

---

## 📄 Licencia

Este proyecto está bajo la licencia **MIT**. Ver archivo [`LICENSE`](LICENSE) para detalles.

**Copyright (c) 2025 Luis Martz**

---

## 📚 Referencias

- [JavaFX Documentation](https://openjfx.io/javadoc/17/)
- [SQLite JDBC](https://github.com/xerial/sqlite-jdbc)
- [Maven Official](https://maven.apache.org/)
- [JUnit 5](https://junit.org/junit5/docs/current/user-guide/)

---

**Última actualización:** Diciembre 2025  
**Versión:** 1.0.0

## Contributing

1. Fork the repository
2. Create a feature branch
3. Open a pull request with tests and a short description
4. Keep UI strings in Spanish consistent with existing labels

## License

Add your preferred project license file (e.g. `LICENSE`) to the repository.

---

**Note**: If more specific run scripts or build files (Maven/Gradle) are desired, they can be added to the repository.
