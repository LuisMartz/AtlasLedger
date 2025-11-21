package atlasledger.ui.dashboard;

import atlasledger.app.AppContext;
import atlasledger.model.Worker;
import atlasledger.service.DatabaseIntegrityService;
import atlasledger.ui.informes.ReportModule;
import atlasledger.ui.ordenes.OrderModule;
import atlasledger.ui.productos.ProductModule;
import atlasledger.ui.proveedores.ProviderModule;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * MainScreen is the primary UI container for the Atlas Ledger application dashboard.
 * <p>
 * It provides a sidebar for navigation between different modules (Productos, Proveedores,
 * Ordenes, Informes, Analitica), a header with global actions (data integrity check,
 * synchronization), and a central content area where the selected module is displayed.
 * </p>
 * <p>
 * Features:
 * <ul>
 *   <li>Sidebar navigation with module icons and tooltips</li>
 *   <li>User card displaying current user's name, role, and mode (local/server)</li>
 *   <li>Header with application title, subtitle, and action buttons</li>
 *   <li>Animated transitions between modules</li>
 *   <li>Logout functionality and database integrity validation</li>
 *   <li>Module content caching for performance</li>
 * </ul>
 * </p>
 *
 * @author  LuisMartz
 * @version 1.0
 */
public class MainScreen extends BorderPane {

    private final AppContext context;
    private final Label subtitle = new Label("Selecciona un modulo para comenzar.");
    private final StackPane contentArea = new StackPane();
    private final Map<String, Supplier<Node>> moduleSuppliers = new LinkedHashMap<>();
    private final Map<String, Node> moduleCache = new HashMap<>();
    private final Map<Button, String> navMapping = new LinkedHashMap<>();
    private Button activeNavButton;
    private final Runnable logoutHandler;

    public MainScreen(AppContext context, Runnable logoutHandler) {
        this.context = context;
        this.logoutHandler = logoutHandler;
        getStyleClass().add("app-shell");
        setPrefWidth(1200);
        setPrefHeight(760);
        initialiseModules();
        setLeft(buildSidebar());
        setCenter(buildMainColumn());
        activateModule("Productos");
    }

    private void initialiseModules() {
        moduleSuppliers.put("Productos", () -> new ProductModule(context));
        moduleSuppliers.put("Proveedores", () -> new ProviderModule(context));
        moduleSuppliers.put("Ordenes", () -> new OrderModule(context));
        moduleSuppliers.put("Informes", () -> new ReportModule(context));
        moduleSuppliers.put("Analitica", () -> new AnalyticsModule(context));
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(12);
        sidebar.getStyleClass().add("sidebar");

        Label brand = new Label("Atlas Ledger");
        brand.getStyleClass().add("sidebar-title");
        sidebar.getChildren().add(brand);
        sidebar.getChildren().add(buildUserCard());

        moduleSuppliers.keySet().forEach(name -> sidebar.getChildren().add(createNavButton(name)));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Button logoutButton = new Button("Cerrar sesion");
        logoutButton.getStyleClass().add("button-logout");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setOnAction(event -> {
            if (context.getSyncService() != null) {
                context.getSyncService().close();
            }
            if (logoutHandler != null) {
                logoutHandler.run();
            }
        });
        sidebar.getChildren().add(logoutButton);

        return sidebar;
    }

    private Button createNavButton(String name) {
        Button button = new Button(name);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        Label icon = new Label(iconFor(name));
        icon.getStyleClass().add("nav-icon");
        button.setGraphic(icon);
        button.setOnAction(event -> activateModule(name));
        button.setTooltip(new Tooltip("Abrir modulo de " + name.toLowerCase()));
        navMapping.put(button, name);
        return button;
    }

    private VBox buildMainColumn() {
        VBox column = new VBox();
        column.getStyleClass().add("content-area");
        column.setSpacing(18);
        column.getChildren().add(buildHeader());

        contentArea.getStyleClass().add("content-stack");
        contentArea.setPadding(Insets.EMPTY);
        contentArea.setCache(true);
        contentArea.setCacheHint(CacheHint.SPEED);
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        column.getChildren().add(contentArea);

        return column;
    }

    private HBox buildHeader() {
        HBox header = new HBox(18);
        header.getStyleClass().add("header-bar");

        Label title = new Label("Atlas Ledger");
        title.getStyleClass().add("app-title");

        subtitle.getStyleClass().add("subtitle");

        VBox caption = new VBox(4, title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button integrity = new Button("Validar datos");
        integrity.getStyleClass().addAll("button", "ghost");
        integrity.setOnAction(event -> showIntegrity());
        integrity.setTooltip(new Tooltip("Ejecutar verificaciones de integridad en SQLite"));

        Button sync = new Button("Sincronizar");
        sync.getStyleClass().addAll("button", "cta");
        sync.setOnAction(event -> context.getSyncService().pushPendingAsync(success -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sincronizacion");
            alert.setHeaderText(null);
            alert.setContentText(success ? "Sincronizacion completada." : "Sin conexion. Intentar mas tarde.");
            alert.showAndWait();
        }));
        sync.setTooltip(new Tooltip("Enviar y recibir cambios con el servidor remoto"));

        header.getChildren().addAll(caption, spacer, integrity, sync);
        return header;
    }

    private void activateModule(String name) {
        Node module = moduleCache.computeIfAbsent(name, key -> moduleSuppliers.get(key).get());
        if (!contentArea.getChildren().contains(module)) {
            module.setOpacity(0);
            contentArea.getChildren().add(module);
        }
        module.setCache(true);
        module.setCacheHint(CacheHint.SPEED);
        module.toFront();

        FadeTransition fade = new FadeTransition(Duration.millis(160), module);
        fade.setInterpolator(Interpolator.EASE_BOTH);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.playFromStart();

        subtitle.setText(subtitleFor(name));
        updateNavigationState(name);
    }

    private void updateNavigationState(String activeName) {
        navMapping.forEach((button, target) -> {
            boolean isActive = target.equals(activeName);
            if (isActive && !button.getStyleClass().contains("nav-button-active")) {
                button.getStyleClass().add("nav-button-active");
                activeNavButton = button;
            } else if (!isActive) {
                button.getStyleClass().remove("nav-button-active");
            }
        });
    }

    private String subtitleFor(String moduleName) {
        return switch (moduleName) {
            case "Productos" -> "Controla tu inventario, costos y margenes en tiempo real.";
            case "Proveedores" -> "Gestiona relaciones y contratos con tus aliados estrategicos.";
            case "Ordenes" -> "Supervisa el ciclo de compras y trazabilidad de pedidos.";
            case "Informes" -> "Genera reportes clave para la toma de decisiones.";
            case "Analitica" -> "Visualiza el rendimiento operativo y la bitacora del sistema.";
            default -> "Selecciona un modulo para comenzar.";
        };
    }

    private String iconFor(String moduleName) {
        return switch (moduleName) {
            case "Productos" -> "\uD83D\uDCBC";
            case "Proveedores" -> "\uD83D\uDC65";
            case "Ordenes" -> "\uD83D\uDCE6";
            case "Informes" -> "\uD83D\uDCC8";
            case "Analitica" -> "\uD83D\uDCCA";
            default -> "\u25A1";
        };
    }

    private VBox buildUserCard() {
        Worker worker = context.getWorker();
        String name = worker != null && worker.getNombre() != null && !worker.getNombre().isBlank()
            ? worker.getNombre()
            : (worker != null ? worker.getUsername() : "Usuario");
        String role = worker != null && worker.getRol() != null && !worker.getRol().isBlank()
            ? worker.getRol()
            : "Colaborador";
        String mode = context.isLocalMode() ? "Modo local" : "Servidor central";

        Label avatar = new Label(extractInitials(name));
        avatar.getStyleClass().add("avatar-circle");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("sidebar-user-name");

        Label roleLabel = new Label(role + " · " + mode);
        roleLabel.getStyleClass().add("sidebar-user-role");

        VBox card = new VBox(4, avatar, nameLabel, roleLabel);
        card.getStyleClass().add("sidebar-user-card");
        return card;
    }

    private String extractInitials(String name) {
        if (name == null || name.isBlank()) {
            return "U";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private void showIntegrity() {
        DatabaseIntegrityService integrityService = context.getDatabaseIntegrityService();
        var issues = integrityService.validarReferencias();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Integridad de Datos");
        alert.setHeaderText(null);
        if (issues.isEmpty()) {
            alert.setContentText("Sin incidencias. La base de datos esta consistente.");
        } else {
            alert.setContentText(String.join("\n", issues));
        }
        alert.showAndWait();
    }
}
