package atlasledger.ui.productos;

import atlasledger.app.AppContext;
import atlasledger.model.Producto;
import atlasledger.repository.ProductRepository;
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

public class ProductModule extends BorderPane {

    private final ProductRepository productRepository;
    private final SyncService syncService;
    private final ObservableList<Producto> data;
    private final TableView<Producto> tableView;

    public ProductModule(AppContext context) {
        this.productRepository = context.getProductRepository();
        this.syncService = context.getSyncService();

        setPadding(new Insets(16));

        Label header = new Label("Catalogo de Productos");
        header.getStyleClass().add("module-title");

        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        data = FXCollections.observableArrayList(productRepository.findAll());
        tableView.setItems(data);

        TableColumn<Producto, String> codigoColumn = new TableColumn<>("Codigo");
        codigoColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCodigo()));

        TableColumn<Producto, String> nombreColumn = new TableColumn<>("Nombre");
        nombreColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNombre()));

        TableColumn<Producto, String> categoriaColumn = new TableColumn<>("Categoria");
        categoriaColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCategoria()));

        TableColumn<Producto, Number> stockColumn = new TableColumn<>("Stock");
        stockColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getStock()));

        TableColumn<Producto, Number> precioColumn = new TableColumn<>("Precio");
        precioColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getPrecio()));

        TableColumn<Producto, Number> margenColumn = new TableColumn<>("Margen");
        margenColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getMargen()));

        tableView.getColumns().setAll(codigoColumn, nombreColumn, categoriaColumn, stockColumn, precioColumn, margenColumn);

        Button nuevoProducto = new Button("Nuevo Producto Demo");
        nuevoProducto.setOnAction(event -> crearProductoDemo());

        Button refrescar = new Button("Actualizar");
        refrescar.setOnAction(event -> recargar());

        Button sincronizar = new Button("Sincronizar");
        sincronizar.setOnAction(event -> syncService.pushPendingAsync(success -> {
            if (success) {
                recargar();
            }
        }));

        HBox acciones = new HBox(8, nuevoProducto, refrescar, sincronizar);
        acciones.setPadding(new Insets(0, 0, 12, 0));

        javafx.scene.layout.VBox top = new javafx.scene.layout.VBox(header, acciones);
        top.setSpacing(8);
        setTop(top);
        setCenter(tableView);
    }

    private void crearProductoDemo() {
        Producto producto = new Producto(
            "P-" + System.currentTimeMillis(),
            "Producto Demo",
            "General",
            "GEN",
            25,
            10.0,
            15.5
        );
        productRepository.save(producto);
        syncService.enqueueChange(
            "productos",
            producto.getCodigo(),
            """
                {"codigo":"%s","nombre":"%s","categoria":"%s","proveedorCodigo":"%s","stock":%d,"coste":%.2f,"precio":%.2f}
            """.formatted(
                producto.getCodigo(),
                producto.getNombre(),
                producto.getCategoria(),
                producto.getProveedorCodigo(),
                producto.getStock(),
                producto.getCoste(),
                producto.getPrecio()
            ),
            SyncService.SyncOperation.CREATE
        );
        recargar();
    }

    private void recargar() {
        data.setAll(productRepository.findAll());
    }
}
