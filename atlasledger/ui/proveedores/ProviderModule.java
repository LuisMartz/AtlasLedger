package atlasledger.ui.proveedores;

import atlasledger.app.AppContext;
import atlasledger.model.Proveedor;
import atlasledger.repository.ProviderRepository;
import atlasledger.service.SyncService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ProviderModule extends BorderPane {

    private final ProviderRepository providerRepository;
    private final SyncService syncService;
    private final ObservableList<Proveedor> data;
    private final TableView<Proveedor> tableView;

    public ProviderModule(AppContext context) {
        this.providerRepository = context.getProviderRepository();
        this.syncService = context.getSyncService();

        setPadding(new Insets(16));

        Label header = new Label("Gestion de Proveedores");
        header.getStyleClass().add("module-title");

        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        data = FXCollections.observableArrayList(providerRepository.findAll());
        tableView.setItems(data);

        TableColumn<Proveedor, String> codigoColumn = new TableColumn<>("Codigo");
        codigoColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCodigo()));

        TableColumn<Proveedor, String> nombreColumn = new TableColumn<>("Nombre");
        nombreColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNombre()));

        TableColumn<Proveedor, String> contactoColumn = new TableColumn<>("Contacto");
        contactoColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getTelefono()));

        TableColumn<Proveedor, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getEmail()));

        tableView.getColumns().setAll(codigoColumn, nombreColumn, contactoColumn, emailColumn);

        Button nuevoProveedor = new Button("Nuevo Proveedor Demo");
        nuevoProveedor.setOnAction(event -> crearProveedorDemo());

        Button refrescar = new Button("Actualizar");
        refrescar.setOnAction(event -> recargar());

        HBox acciones = new HBox(8, nuevoProveedor, refrescar);
        acciones.setPadding(new Insets(0, 0, 12, 0));

        VBox top = new VBox(header, acciones);
        top.setSpacing(8);
        setTop(top);
        setCenter(tableView);
    }

    private void crearProveedorDemo() {
        Proveedor proveedor = new Proveedor(
            "PV-" + System.currentTimeMillis(),
            "Proveedor Demo",
            "demo@atlasledger.test",
            "+34 600 00 00",
            "Calle Demo 123"
        );
        providerRepository.save(proveedor);
        syncService.enqueueChange(
            "proveedores",
            proveedor.getCodigo(),
            """
                {"codigo":"%s","nombre":"%s","email":"%s","telefono":"%s","direccion":"%s"}
            """.formatted(
                proveedor.getCodigo(),
                proveedor.getNombre(),
                proveedor.getEmail(),
                proveedor.getTelefono(),
                proveedor.getDireccion()
            ),
            SyncService.SyncOperation.CREATE
        );
        recargar();
    }

    private void recargar() {
        data.setAll(providerRepository.findAll());
    }
}
