package atlasledger.ui.ordenes;

import atlasledger.app.AppContext;
import atlasledger.model.Orden;
import atlasledger.repository.OrderRepository;
import atlasledger.repository.ProviderRepository;
import atlasledger.service.SyncService;
import java.time.LocalDate;
import java.util.Optional;
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

public class OrderModule extends BorderPane {

    private final OrderRepository orderRepository;
    private final ProviderRepository providerRepository;
    private final SyncService syncService;
    private final ObservableList<Orden> data;
    private final TableView<Orden> tableView;

    public OrderModule(AppContext context) {
        this.orderRepository = context.getOrderRepository();
        this.providerRepository = context.getProviderRepository();
        this.syncService = context.getSyncService();

        setPadding(new Insets(16));

        Label header = new Label("Ordenes de Compra");
        header.getStyleClass().add("module-title");

        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        data = FXCollections.observableArrayList(orderRepository.findAll());
        tableView.setItems(data);

        TableColumn<Orden, String> codigoColumn = new TableColumn<>("Codigo");
        codigoColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCodigo()));

        TableColumn<Orden, String> fechaColumn = new TableColumn<>("Fecha");
        fechaColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            Optional.ofNullable(cell.getValue().getFecha()).map(LocalDate::toString).orElse("")));

        TableColumn<Orden, String> proveedorColumn = new TableColumn<>("Proveedor");
        proveedorColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getProveedorCodigo()));

        TableColumn<Orden, String> estadoColumn = new TableColumn<>("Estado");
        estadoColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            Optional.ofNullable(cell.getValue().getEstado()).map(Enum::name).orElse("")));

        TableColumn<Orden, Number> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getTotal()));

        tableView.getColumns().setAll(codigoColumn, fechaColumn, proveedorColumn, estadoColumn, totalColumn);

        Button nuevaOrden = new Button("Nueva Orden Demo");
        nuevaOrden.setOnAction(event -> crearOrdenDemo());

        Button refrescar = new Button("Actualizar");
        refrescar.setOnAction(event -> recargar());

        HBox acciones = new HBox(8, nuevaOrden, refrescar);
        acciones.setPadding(new Insets(0, 0, 12, 0));

        VBox top = new VBox(header, acciones);
        top.setSpacing(8);
        setTop(top);
        setCenter(tableView);
    }

    private void crearOrdenDemo() {
        String proveedorCodigo = providerRepository.findAll().stream()
            .findFirst()
            .map(proveedor -> proveedor.getCodigo())
            .orElse("GEN");

        Orden orden = new Orden(
            "OC-" + System.currentTimeMillis(),
            LocalDate.now(),
            proveedorCodigo,
            150.75,
            Orden.Estado.APROBADA
        );

        orderRepository.save(orden);
        syncService.enqueueChange(
            "ordenes",
            orden.getCodigo(),
            """
                {"codigo":"%s","fecha":"%s","proveedorCodigo":"%s","total":%.2f,"estado":"%s"}
            """.formatted(
                orden.getCodigo(),
                orden.getFecha(),
                orden.getProveedorCodigo(),
                orden.getTotal(),
                orden.getEstado()
            ),
            SyncService.SyncOperation.CREATE
        );
        recargar();
    }

    private void recargar() {
        data.setAll(orderRepository.findAll());
    }
}
