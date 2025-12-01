package atlasledger.ui.ordenes;

import atlasledger.model.Orden;
import atlasledger.model.Proveedor;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class OrderDialog extends Dialog<Orden> {

    private final TextField codigoField = new TextField();
    private final DatePicker fechaPicker = new DatePicker(LocalDate.now());
    private final ComboBox<String> proveedorCombo;
    private final ComboBox<Orden.Estado> estadoCombo = new ComboBox<>();
    private final TextField totalField = new TextField();

    public OrderDialog(List<Proveedor> proveedores, Orden existing) {
        boolean editing = existing != null;
        setTitle(editing ? "Editar orden" : "Nueva orden");
        setHeaderText(editing ? "Actualiza los datos de la orden de compra." : "Introduce los datos de la nueva orden de compra.");

        ObservableList<String> proveedorOptions = FXCollections.observableArrayList(
            proveedores.stream().map(Proveedor::getCodigo).collect(Collectors.toList())
        );
        proveedorCombo = new ComboBox<>(proveedorOptions);
        proveedorCombo.setEditable(false);

        estadoCombo.setItems(FXCollections.observableArrayList(Orden.Estado.values()));

        ButtonType saveType = new ButtonType(editing ? "Guardar" : "Crear", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = buildForm();
        populate(existing);
        getDialogPane().setContent(grid);

        getDialogPane().lookupButton(saveType).disableProperty().bind(
            codigoField.textProperty().isEmpty()
                .or(proveedorCombo.getSelectionModel().selectedItemProperty().isNull())
                .or(totalField.textProperty().isEmpty())
        );

        setResultConverter(button -> {
            if (button == saveType) {
                Orden orden = Optional.ofNullable(existing).map(OrderDialog::cloneOrden).orElseGet(Orden::new);
                orden.setCodigo(codigoField.getText().trim());
                orden.setFecha(fechaPicker.getValue());
                orden.setProveedorCodigo(proveedorCombo.getValue());
                orden.setTotal(parseDouble(totalField.getText(), existing != null ? existing.getTotal() : 0.0));
                orden.setEstado(Optional.ofNullable(estadoCombo.getValue()).orElse(Orden.Estado.BORRADOR));
                return orden;
            }
            return null;
        });
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        int row = 0;
        grid.add(new Label("Codigo"), 0, row);
        grid.add(codigoField, 1, row++);

        grid.add(new Label("Fecha"), 0, row);
        grid.add(fechaPicker, 1, row++);

        grid.add(new Label("Proveedor"), 0, row);
        grid.add(proveedorCombo, 1, row++);

        grid.add(new Label("Estado"), 0, row);
        grid.add(estadoCombo, 1, row++);

        grid.add(new Label("Total"), 0, row);
        grid.add(totalField, 1, row);

        totalField.setPromptText("0.00");

        return grid;
    }

    private void populate(Orden existing) {
        if (existing != null) {
            codigoField.setText(existing.getCodigo());
            codigoField.setDisable(true);
            fechaPicker.setValue(Optional.ofNullable(existing.getFecha()).orElse(LocalDate.now()));
            if (existing.getProveedorCodigo() != null) {
                proveedorCombo.getSelectionModel().select(existing.getProveedorCodigo());
            }
            estadoCombo.getSelectionModel().select(Optional.ofNullable(existing.getEstado()).orElse(Orden.Estado.BORRADOR));
            totalField.setText(String.valueOf(existing.getTotal()));
        } else if (!proveedorCombo.getItems().isEmpty()) {
            proveedorCombo.getSelectionModel().selectFirst();
            estadoCombo.getSelectionModel().select(Orden.Estado.BORRADOR);
        }
    }

    private static Orden cloneOrden(Orden original) {
        Orden copy = new Orden();
        copy.setId(original.getId());
        copy.setCodigo(original.getCodigo());
        copy.setFecha(original.getFecha());
        copy.setProveedorCodigo(original.getProveedorCodigo());
        copy.setTotal(original.getTotal());
        copy.setEstado(original.getEstado());
        copy.setActualizadoEn(original.getActualizadoEn());
        return copy;
    }

    private double parseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
