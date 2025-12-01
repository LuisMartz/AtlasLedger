package atlasledger.ui.ordenes;

import atlasledger.app.AppContext;
import atlasledger.model.Orden;
import atlasledger.model.Proveedor;
import atlasledger.repository.OrderRepository;
import atlasledger.repository.ProviderRepository;
import atlasledger.service.SyncService;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
import javafx.util.StringConverter;

public class OrderModule extends BorderPane {

    private final OrderRepository orderRepository;
    private final ProviderRepository providerRepository;
    private final SyncService syncService;
    private final ObservableList<Orden> data;
    private final TableView<Orden> tableView;
    private final Label totalOrdenesValue = new Label();
    private final Label pendientesValue = new Label();
    private final Label totalComprasValue = new Label();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("es", "ES"));

    public OrderModule(AppContext context) {
        this.orderRepository = context.getOrderRepository();
        this.providerRepository = context.getProviderRepository();
        this.syncService = context.getSyncService();

        data = FXCollections.observableArrayList(orderRepository.findAll());
        FilteredList<Orden> filtered = new FilteredList<>(data, orden -> true);
        SortedList<Orden> sorted = new SortedList<>(filtered);
        tableView = buildTable(sorted);
        sorted.comparatorProperty().bind(tableView.comparatorProperty());

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por codigo o proveedor...");

        ComboBox<Orden.Estado> estadoFilter = new ComboBox<>();
        estadoFilter.getItems().add(null);
        estadoFilter.getItems().addAll(EnumSet.allOf(Orden.Estado.class));
        estadoFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(Orden.Estado estado) {
                return formatEstado(estado);
            }

            @Override
            public Orden.Estado fromString(String string) {
                return null;
            }
        });
        estadoFilter.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Orden.Estado item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : formatEstado(item));
            }
        });
        estadoFilter.setPromptText("Estado");

        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters(filtered, searchField.getText(), estadoFilter.getValue()));
        estadoFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters(filtered, searchField.getText(), newValue));

        Label header = new Label("Ordenes de compra");
        header.getStyleClass().add("module-title");

        Label description = new Label("Controla el ciclo de aprobacion y recepcion de tus pedidos.");
        description.getStyleClass().add("module-description");

        VBox heading = new VBox(4, header, description);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerRow = new HBox(16, heading, spacer, estadoFilter, searchField);
        headerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        HBox statsBar = buildStatsBar();

        Button newButton = new Button("Nueva orden");
        newButton.getStyleClass().add("cta");
        newButton.setOnAction(event -> openDialog(null));

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
                Alert alert = new Alert(Alert.AlertType.WARNING, "Sin conexion. Intenta nuevamente mas tarde.");
                alert.initOwner(getScene() != null ? getScene().getWindow() : null);
                alert.showAndWait();
            }
        })));
        syncButton.setTooltip(new Tooltip("Sincronizar ordenes con el ERP central."));

        HBox toolbar = new HBox(8, newButton, editButton, deleteButton, refreshButton, syncButton);
        toolbar.getStyleClass().add("module-toolbar");

        VBox wrapper = new VBox(18, headerRow, statsBar, toolbar, tableView);
        wrapper.getStyleClass().add("module-wrapper");
        VBox.setVgrow(tableView, Priority.ALWAYS);
        setCenter(wrapper);

        updateStats();
    }

    private void applyFilters(FilteredList<Orden> filtered, String search, Orden.Estado estado) {
        String normalized = search == null ? "" : search.trim().toLowerCase(Locale.getDefault());
        filtered.setPredicate(orden -> {
            boolean matchesEstado = estado == null || estado == orden.getEstado();
            if (!matchesEstado) {
                return false;
            }
            if (normalized.isEmpty()) {
                return true;
            }
            return orden.getCodigo().toLowerCase(Locale.getDefault()).contains(normalized)
                || Optional.ofNullable(orden.getProveedorCodigo()).orElse("").toLowerCase(Locale.getDefault()).contains(normalized);
        });
    }

    private TableView<Orden> buildTable(SortedList<Orden> items) {
        TableView<Orden> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Orden, String> codigoColumn = new TableColumn<>("Codigo");
        codigoColumn.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        TableColumn<Orden, LocalDate> fechaColumn = new TableColumn<>("Fecha");
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        TableColumn<Orden, String> proveedorColumn = new TableColumn<>("Proveedor");
        proveedorColumn.setCellValueFactory(new PropertyValueFactory<>("proveedorCodigo"));

        TableColumn<Orden, Orden.Estado> estadoColumn = new TableColumn<>("Estado");
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));

        TableColumn<Orden, Number> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

        table.getColumns().setAll(codigoColumn, fechaColumn, proveedorColumn, estadoColumn, totalColumn);
        table.setRowFactory(tv -> {
            TableRow<Orden> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openDialog(row.getItem());
                }
            });
            return row;
        });
        table.setPlaceholder(new Label("Registra tus ordenes para comenzar a rastrear compras."));
        table.setItems(items);
        return table;
    }

    private HBox buildStatsBar() {
        HBox stats = new HBox(16);
        stats.getStyleClass().add("stats-bar");
        stats.getChildren().addAll(
            statCard("Ordenes registradas", totalOrdenesValue, null),
            statCard("Pendientes", pendientesValue, "accent-orange"),
            statCard("Monto total", totalComprasValue, "accent-green")
        );
        return stats;
    }

    private VBox statCard(String title, Label value, String accent) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-card-label");
        value.getStyleClass().add("stat-card-value");
        value.setText("--");
        VBox card = new VBox(2, titleLabel, value);
        card.getStyleClass().add("stat-card");
        if (accent != null) {
            card.getStyleClass().add(accent);
        }
        return card;
    }

    private String formatEstado(Orden.Estado estado) {
        if (estado == null) {
            return "Todos";
        }
        return switch (estado) {
            case BORRADOR -> "Borrador";
            case APROBADA -> "Aprobada";
            case ENVIADA -> "Enviada";
            case RECIBIDA -> "Recibida";
            case CANCELADA -> "Cancelada";
        };
    }

    private void openDialog(Orden orden) {
        List<Proveedor> proveedores = providerRepository.findAll();
        if (proveedores.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sin proveedores");
            alert.setHeaderText(null);
            alert.setContentText("Registra al menos un proveedor antes de crear ordenes.");
            alert.initOwner(getScene() != null ? getScene().getWindow() : null);
            alert.showAndWait();
            return;
        }
        OrderDialog dialog = new OrderDialog(proveedores, orden);
        dialog.initOwner(getScene() != null ? getScene().getWindow() : null);
        dialog.showAndWait().ifPresent(updated -> {
            SyncService.SyncOperation op = orden == null ? SyncService.SyncOperation.CREATE : SyncService.SyncOperation.UPDATE;
            orderRepository.save(updated);
            syncService.enqueueChange("ordenes", updated.getCodigo(), toJson(updated), op);
            recargar();
        });
    }

    private void eliminarSeleccion() {
        Orden selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar orden");
        confirm.setHeaderText(null);
        confirm.setContentText("Deseas eliminar la orden " + selected.getCodigo() + "?");
        confirm.initOwner(getScene() != null ? getScene().getWindow() : null);
        confirm.showAndWait()
            .filter(result -> result == javafx.scene.control.ButtonType.OK)
            .ifPresent(result -> {
                orderRepository.deleteById(selected.getId());
                syncService.enqueueChange("ordenes", selected.getCodigo(), "{\"codigo\":\"" + selected.getCodigo() + "\"}", SyncService.SyncOperation.DELETE);
                recargar();
            });
    }

    private String toJson(Orden orden) {
        return String.format(Locale.US,
            "{\"codigo\":\"%s\",\"fecha\":\"%s\",\"proveedorCodigo\":\"%s\",\"total\":%.2f,\"estado\":\"%s\"}",
            orden.getCodigo(),
            Optional.ofNullable(orden.getFecha()).map(LocalDate::toString).orElse(""),
            Optional.ofNullable(orden.getProveedorCodigo()).orElse(""),
            orden.getTotal(),
            Optional.ofNullable(orden.getEstado()).map(Enum::name).orElse(Orden.Estado.BORRADOR.name())
        );
    }

    private void recargar() {
        data.setAll(orderRepository.findAll());
        updateStats();
    }

    private void updateStats() {
        long pendientes = data.stream()
            .filter(orden -> orden.getEstado() != null && (orden.getEstado() == Orden.Estado.APROBADA || orden.getEstado() == Orden.Estado.ENVIADA))
            .count();
        double totalCompras = data.stream().mapToDouble(Orden::getTotal).sum();

        totalOrdenesValue.setText(numberFormat.format(data.size()));
        pendientesValue.setText(numberFormat.format(pendientes));
        totalComprasValue.setText(currencyFormat.format(totalCompras));
    }
}
