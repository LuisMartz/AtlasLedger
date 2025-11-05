package atlasledger.ui.dashboard;

import atlasledger.app.AppContext;
import atlasledger.service.DatabaseIntegrityService;
import atlasledger.ui.informes.ReportModule;
import atlasledger.ui.ordenes.OrderModule;
import atlasledger.ui.productos.ProductModule;
import atlasledger.ui.proveedores.ProviderModule;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class MainScreen extends BorderPane {

    private final AppContext context;

    public MainScreen(AppContext context) {
        this.context = context;
        setPadding(new Insets(12));
        setPrefWidth(1100);
        setPrefHeight(680);
        configureHeader();
        configureTabs();
    }

    private void configureHeader() {
        Label title = new Label("Atlas Ledger - ERP Lite");
        title.getStyleClass().add("app-title");

        Button integrity = new Button("Validar Datos");
        integrity.setOnAction(event -> showIntegrity());

        Button sync = new Button("Sincronizar");
        sync.setOnAction(event -> context.getSyncService().pushPendingAsync(success -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sincronizacion");
            alert.setHeaderText(null);
            alert.setContentText(success ? "Sincronizacion completada." : "Sin conexion. Intentar mas tarde.");
            alert.showAndWait();
        }));

        HBox header = new HBox(12, title, integrity, sync);
        header.setPadding(new Insets(0, 0, 12, 0));
        setTop(header);
    }

    private void configureTabs() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab products = new Tab("Productos", new ProductModule(context));
        Tab providers = new Tab("Proveedores", new ProviderModule(context));
        Tab orders = new Tab("Ordenes", new OrderModule(context));
        Tab reports = new Tab("Informes", new ReportModule(context));

        tabs.getTabs().setAll(products, providers, orders, reports);
        setCenter(tabs);
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
