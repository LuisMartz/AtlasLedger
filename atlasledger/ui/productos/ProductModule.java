package atlasledger.ui.productos;

import atlasledger.app.AppContext;
import atlasledger.model.Producto;
import atlasledger.repository.ProductRepository;
import atlasledger.service.SyncService;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ProductModule extends BorderPane {

    private final ProductRepository productRepository;
    private final SyncService syncService;
    private final ObservableList<Producto> data;
    private final TableView<Producto> tableView;
    private final Label totalProductosValue = new Label();
    private final Label stockTotalValue = new Label();
    private final Label valorInventarioValue = new Label();
    private final Label margenPromedioValue = new Label();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("es", "ES"));

    public ProductModule(AppContext context) {
        this.productRepository = context.getProductRepository();
        this.syncService = context.getSyncService();

        data = FXCollections.observableArrayList(productRepository.findAll());
        FilteredList<Producto> filtered = new FilteredList<>(data, producto -> true);
        SortedList<Producto> sorted = new SortedList<>(filtered);
        tableView = buildTable(sorted);
        sorted.comparatorProperty().bind(tableView.comparatorProperty());

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por codigo, nombre o categoria...");
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            String filter = newValue == null ? "" : newValue.trim().toLowerCase(Locale.getDefault());
            filtered.setPredicate(producto -> {
                if (filter.isEmpty()) {
                    return true;
                }
                return producto.getCodigo().toLowerCase(Locale.getDefault()).contains(filter)
                    || producto.getNombre().toLowerCase(Locale.getDefault()).contains(filter)
                    || Optional.ofNullable(producto.getCategoria()).orElse("").toLowerCase(Locale.getDefault()).contains(filter);
            });
        });

        Label header = new Label("Catalogo de productos");
        header.getStyleClass().add("module-title");

        Label description = new Label("Administra tu inventario, precios de venta y niveles de stock.");
        description.getStyleClass().add("module-description");

        VBox heading = new VBox(4, header, description);
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox headerRow = new HBox(16, heading, headerSpacer, searchField);
        headerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        HBox statsBar = buildStatsBar();

        Button addButton = new Button("Nuevo producto");
        addButton.getStyleClass().add("cta");
        addButton.setGraphicTextGap(6);
        addButton.setOnAction(event -> openDialog(null));

        Button editButton = new Button("Editar");
        editButton.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
        editButton.setOnAction(event -> openDialog(tableView.getSelectionModel().getSelectedItem()));

        Button deleteButton = new Button("Eliminar");
        deleteButton.getStyleClass().add("destructive");
        deleteButton.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.setOnAction(event -> eliminarSeleccion());

        Button refreshButton = new Button("Actualizar");
        refreshButton.setOnAction(event -> recargar());

        Button syncButton = new Button("Sincronizar");
        syncButton.setOnAction(event -> syncService.pushPendingAsync(success -> Platform.runLater(() -> {
            if (success) {
                recargar();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "No fue posible sincronizar. Revisa la conexion e intenta de nuevo.");
                alert.initOwner(getScene() != null ? getScene().getWindow() : null);
                alert.showAndWait();
            }
        })));
        syncButton.setTooltip(new Tooltip("Enviar y recibir movimientos de inventario."));

        HBox toolbar = new HBox(8, addButton, editButton, deleteButton, refreshButton, syncButton);
        toolbar.getStyleClass().add("module-toolbar");

        VBox wrapper = new VBox(18, headerRow, statsBar, toolbar, tableView);
        wrapper.getStyleClass().add("module-wrapper");
        VBox.setVgrow(tableView, Priority.ALWAYS);
        wrapper.setFillWidth(true);

        setCenter(wrapper);
        updateStats();
    }

    private TableView<Producto> buildTable(SortedList<Producto> items) {
        TableView<Producto> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Producto, String> codigoColumn = new TableColumn<>("Codigo");
        codigoColumn.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        TableColumn<Producto, String> nombreColumn = new TableColumn<>("Nombre");
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<Producto, String> categoriaColumn = new TableColumn<>("Categoria");
        categoriaColumn.setCellValueFactory(new PropertyValueFactory<>("categoria"));

        TableColumn<Producto, Number> stockColumn = new TableColumn<>("Stock");
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));

        TableColumn<Producto, Number> costeColumn = new TableColumn<>("Coste");
        costeColumn.setCellValueFactory(new PropertyValueFactory<>("coste"));

        TableColumn<Producto, Number> precioColumn = new TableColumn<>("Precio");
        precioColumn.setCellValueFactory(new PropertyValueFactory<>("precio"));

        TableColumn<Producto, Number> margenColumn = new TableColumn<>("Margen");
        margenColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getMargen()));

        table.getColumns().setAll(codigoColumn, nombreColumn, categoriaColumn, stockColumn, costeColumn, precioColumn, margenColumn);
        table.setRowFactory(tv -> {
            TableRow<Producto> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openDialog(row.getItem());
                }
            });
            return row;
        });
        table.setPlaceholder(new Label("Comienza registrando tus productos principales."));
        table.setItems(items);
        return table;
    }

    private HBox buildStatsBar() {
        HBox stats = new HBox(16);
        stats.getStyleClass().add("stats-bar");

        stats.getChildren().addAll(
            statCard("Productos", totalProductosValue, null),
            statCard("Stock total", stockTotalValue, "accent-green"),
            statCard("Valor inventario", valorInventarioValue, "accent-orange"),
            statCard("Margen promedio", margenPromedioValue, null)
        );

        return stats;
    }

    private VBox statCard(String label, Label valueLabel, String accent) {
        valueLabel.getStyleClass().add("stat-card-value");
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("stat-card-label");

        VBox card = new VBox(2, labelNode, valueLabel);
        card.getStyleClass().add("stat-card");
        if (!"accent-blue".equals(accent)) {
            card.getStyleClass().add(accent);
        }
        valueLabel.setText("--");
        return card;
    }

    private void openDialog(Producto producto) {
        ProductDialog dialog = new ProductDialog(producto);
        dialog.initOwner(getScene().getWindow());
        Optional<Producto> result = dialog.showAndWait();
        result.ifPresent(updated -> {
            SyncService.SyncOperation operation = producto == null
                ? SyncService.SyncOperation.CREATE
                : SyncService.SyncOperation.UPDATE;
            productRepository.save(updated);
            syncService.enqueueChange("productos", updated.getCodigo(), toJson(updated), operation);
            recargar();
        });
    }

    private void eliminarSeleccion() {
        Producto seleccionado = tableView.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar producto");
        confirm.setHeaderText(null);
        confirm.setContentText("Deseas eliminar el producto " + seleccionado.getNombre() + "?");
        confirm.initOwner(getScene().getWindow());

        confirm.showAndWait().filter(response -> response == javafx.scene.control.ButtonType.OK).ifPresent(response -> {
            productRepository.deleteById(seleccionado.getId());
            syncService.enqueueChange("productos", seleccionado.getCodigo(), "{\"codigo\":\"" + seleccionado.getCodigo() + "\"}", SyncService.SyncOperation.DELETE);
            recargar();
        });
    }

    private String toJson(Producto producto) {
        return String.format(Locale.US,
            "{\"codigo\":\"%s\",\"nombre\":\"%s\",\"categoria\":\"%s\",\"proveedorCodigo\":\"%s\",\"stock\":%d,\"coste\":%.2f,\"precio\":%.2f}",
            producto.getCodigo(),
            producto.getNombre(),
            Optional.ofNullable(producto.getCategoria()).orElse(""),
            Optional.ofNullable(producto.getProveedorCodigo()).orElse(""),
            producto.getStock(),
            producto.getCoste(),
            producto.getPrecio()
        );
    }

    private void recargar() {
        data.setAll(productRepository.findAll());
        updateStats();
    }

    private void updateStats() {
        int totalProductos = data.size();
        int stockTotal = data.stream().mapToInt(Producto::getStock).sum();
        double valorInventario = data.stream().mapToDouble(p -> p.getPrecio() * p.getStock()).sum();
        double margenPromedio = data.isEmpty() ? 0.0 : data.stream().mapToDouble(Producto::getMargen).average().orElse(0.0);

        totalProductosValue.setText(numberFormat.format(totalProductos));
        stockTotalValue.setText(numberFormat.format(stockTotal));
        valorInventarioValue.setText(currencyFormat.format(valorInventario));
        margenPromedioValue.setText(currencyFormat.format(margenPromedio));
    }
}
