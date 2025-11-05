package atlasledger.ui.proveedores;

import atlasledger.app.AppContext;
import atlasledger.model.Proveedor;
import atlasledger.repository.ProviderRepository;
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

public class ProviderModule extends BorderPane {

    private final ProviderRepository providerRepository;
    private final SyncService syncService;
    private final ObservableList<Proveedor> data;
    private final TableView<Proveedor> tableView;
    private final Label totalProveedoresValue = new Label();
    private final Label conEmailValue = new Label();
    private final Label conTelefonoValue = new Label();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("es", "ES"));

    public ProviderModule(AppContext context) {
        this.providerRepository = context.getProviderRepository();
        this.syncService = context.getSyncService();

        data = FXCollections.observableArrayList(providerRepository.findAll());
        FilteredList<Proveedor> filtered = new FilteredList<>(data, proveedor -> true);
        SortedList<Proveedor> sorted = new SortedList<>(filtered);

        tableView = buildTable(sorted);
        sorted.comparatorProperty().bind(tableView.comparatorProperty());

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por codigo, nombre o ciudad...");
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            String filter = newValue == null ? "" : newValue.trim().toLowerCase(Locale.getDefault());
            filtered.setPredicate(proveedor -> {
                if (filter.isEmpty()) {
                    return true;
                }
                return proveedor.getCodigo().toLowerCase(Locale.getDefault()).contains(filter)
                    || proveedor.getNombre().toLowerCase(Locale.getDefault()).contains(filter)
                    || Optional.ofNullable(proveedor.getDireccion()).orElse("").toLowerCase(Locale.getDefault()).contains(filter);
            });
        });

        Label header = new Label("Gestion de proveedores");
        header.getStyleClass().add("module-title");

        Label description = new Label("Centraliza contactos, condiciones y SLA de tus aliados comerciales.");
        description.getStyleClass().add("module-description");

        VBox heading = new VBox(4, header, description);
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox headerRow = new HBox(16, heading, headerSpacer, searchField);
        headerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        HBox statsBar = buildStatsBar();

        Button addButton = new Button("Nuevo proveedor");
        addButton.getStyleClass().add("cta");
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
                Alert alert = new Alert(Alert.AlertType.WARNING, "Sin conexion. Reintenta la sincronizacion cuando vuelvas a estar en linea.");
                alert.initOwner(getScene() != null ? getScene().getWindow() : null);
                alert.showAndWait();
            }
        })));
        syncButton.setTooltip(new Tooltip("Subir los cambios locales al servidor."));

        HBox toolbar = new HBox(8, addButton, editButton, deleteButton, refreshButton, syncButton);
        toolbar.getStyleClass().add("module-toolbar");

        VBox wrapper = new VBox(18, headerRow, statsBar, toolbar, tableView);
        wrapper.getStyleClass().add("module-wrapper");
        VBox.setVgrow(tableView, Priority.ALWAYS);
        setCenter(wrapper);

        updateStats();
    }

    private TableView<Proveedor> buildTable(SortedList<Proveedor> items) {
        TableView<Proveedor> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Proveedor, String> codigoColumn = new TableColumn<>("Codigo");
        codigoColumn.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        TableColumn<Proveedor, String> nombreColumn = new TableColumn<>("Nombre");
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<Proveedor, String> telefonoColumn = new TableColumn<>("Telefono");
        telefonoColumn.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        TableColumn<Proveedor, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Proveedor, String> direccionColumn = new TableColumn<>("Direccion");
        direccionColumn.setCellValueFactory(new PropertyValueFactory<>("direccion"));

        table.getColumns().setAll(codigoColumn, nombreColumn, telefonoColumn, emailColumn, direccionColumn);
        table.setRowFactory(tv -> {
            TableRow<Proveedor> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openDialog(row.getItem());
                }
            });
            return row;
        });
        table.setPlaceholder(new Label("Agrega tus primeros proveedores para comenzar."));
        table.setItems(items);
        return table;
    }

    private HBox buildStatsBar() {
        HBox stats = new HBox(16);
        stats.getStyleClass().add("stats-bar");
        stats.getChildren().addAll(
            statCard("Proveedores activos", totalProveedoresValue, null),
            statCard("Con email", conEmailValue, "accent-green"),
            statCard("Con telefono", conTelefonoValue, "accent-orange")
        );
        return stats;
    }

    private VBox statCard(String label, Label value, String accent) {
        Label title = new Label(label);
        title.getStyleClass().add("stat-card-label");
        value.getStyleClass().add("stat-card-value");
        value.setText("--");
        VBox card = new VBox(2, title, value);
        card.getStyleClass().add("stat-card");
        if (accent != null) {
            card.getStyleClass().add(accent);
        }
        return card;
    }

    private void openDialog(Proveedor proveedor) {
        ProviderDialog dialog = new ProviderDialog(proveedor);
        dialog.initOwner(getScene() != null ? getScene().getWindow() : null);
        dialog.showAndWait().ifPresent(updated -> {
            SyncService.SyncOperation op = proveedor == null ? SyncService.SyncOperation.CREATE : SyncService.SyncOperation.UPDATE;
            providerRepository.save(updated);
            syncService.enqueueChange("proveedores", updated.getCodigo(), toJson(updated), op);
            recargar();
        });
    }

    private void eliminarSeleccion() {
        Proveedor seleccionado = tableView.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar proveedor");
        confirm.setHeaderText(null);
        confirm.setContentText("Deseas eliminar el proveedor " + seleccionado.getNombre() + "?");
        confirm.initOwner(getScene() != null ? getScene().getWindow() : null);
        confirm.showAndWait()
            .filter(result -> result == javafx.scene.control.ButtonType.OK)
            .ifPresent(result -> {
                providerRepository.deleteById(seleccionado.getId());
                syncService.enqueueChange("proveedores", seleccionado.getCodigo(), "{\"codigo\":\"" + seleccionado.getCodigo() + "\"}", SyncService.SyncOperation.DELETE);
                recargar();
            });
    }

    private String toJson(Proveedor proveedor) {
        return """
            {"codigo":"%s","nombre":"%s","email":"%s","telefono":"%s","direccion":"%s"}
        """.formatted(
            proveedor.getCodigo(),
            proveedor.getNombre(),
            Optional.ofNullable(proveedor.getEmail()).orElse(""),
            Optional.ofNullable(proveedor.getTelefono()).orElse(""),
            Optional.ofNullable(proveedor.getDireccion()).orElse("")
        );
    }

    private void recargar() {
        data.setAll(providerRepository.findAll());
        updateStats();
    }

    private void updateStats() {
        long conEmail = data.stream()
            .map(Proveedor::getEmail)
            .filter(email -> email != null && !email.isBlank())
            .count();
        long conTelefono = data.stream()
            .map(Proveedor::getTelefono)
            .filter(t -> t != null && !t.isBlank())
            .count();

        totalProveedoresValue.setText(numberFormat.format(data.size()));
        conEmailValue.setText(numberFormat.format(conEmail));
        conTelefonoValue.setText(numberFormat.format(conTelefono));
    }
}
