package atlasledger.ui.productos;

import atlasledger.model.Producto;
import atlasledger.model.Proveedor;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class ProductDialog extends Dialog<Producto> {

    private final TextField codigoField = new TextField();
    private final TextField nombreField = new TextField();
    private final TextField categoriaField = new TextField();
    private final ComboBox<String> proveedorCombo;
    private final TextField stockField = new TextField();
    private final TextField costeField = new TextField();
    private final TextField precioField = new TextField();

    public ProductDialog(Producto existing, List<Proveedor> proveedores) {
        boolean editing = existing != null;
        setTitle(editing ? "Editar producto" : "Nuevo producto");
        setHeaderText(editing ? "Actualiza los datos del producto." : "Introduce los datos del nuevo producto.");

        proveedorCombo = new ComboBox<>();
        proveedorCombo.getItems().setAll(
            proveedores.stream()
                .map(Proveedor::getCodigo)
                .sorted()
                .collect(Collectors.toList())
        );
        proveedorCombo.setPromptText(proveedores.isEmpty() ? "Sin proveedores" : "Selecciona proveedor");

        ButtonType saveType = new ButtonType(editing ? "Guardar" : "Crear", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane form = buildForm();
        populateFields(existing);

        getDialogPane().setContent(form);

        Button saveButton = (Button) getDialogPane().lookupButton(saveType);
        saveButton.disableProperty().bind(codigoField.textProperty().isEmpty()
            .or(nombreField.textProperty().isEmpty())
            .or(precioField.textProperty().isEmpty()));

        setResultConverter(button -> {
            if (button == saveType) {
                Producto producto = Optional.ofNullable(existing).map(ProductDialog::cloneProducto).orElseGet(Producto::new);
                producto.setCodigo(codigoField.getText().trim());
                producto.setNombre(nombreField.getText().trim());
                producto.setCategoria(categoriaField.getText().trim());
                producto.setProveedorCodigo(Optional.ofNullable(proveedorCombo.getValue()).orElse("").trim());
                producto.setStock(parseInt(stockField.getText(), existing != null ? existing.getStock() : 0));
                producto.setCoste(parseDouble(costeField.getText(), existing != null ? existing.getCoste() : 0.0));
                producto.setPrecio(parseDouble(precioField.getText(), existing != null ? existing.getPrecio() : 0.0));
                return producto;
            }
            return null;
        });
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10, 10, 10, 10));

        int row = 0;
        grid.add(new Label("Codigo"), 0, row);
        grid.add(codigoField, 1, row++);

        grid.add(new Label("Nombre"), 0, row);
        grid.add(nombreField, 1, row++);

        grid.add(new Label("Categoria"), 0, row);
        grid.add(categoriaField, 1, row++);

        grid.add(new Label("Proveedor"), 0, row);
        grid.add(proveedorCombo, 1, row++);

        grid.add(new Label("Stock"), 0, row);
        grid.add(stockField, 1, row++);

        grid.add(new Label("Coste"), 0, row);
        grid.add(costeField, 1, row++);

        grid.add(new Label("Precio"), 0, row);
        grid.add(precioField, 1, row);

        stockField.setPromptText("0");
        costeField.setPromptText("0.00");
        precioField.setPromptText("0.00");

        return grid;
    }

    private void populateFields(Producto existing) {
        if (existing != null) {
            codigoField.setText(existing.getCodigo());
            codigoField.setDisable(true);
            nombreField.setText(existing.getNombre());
            categoriaField.setText(Optional.ofNullable(existing.getCategoria()).orElse(""));
            proveedorCombo.getSelectionModel().select(Optional.ofNullable(existing.getProveedorCodigo()).orElse(""));
            stockField.setText(String.valueOf(existing.getStock()));
            costeField.setText(String.valueOf(existing.getCoste()));
            precioField.setText(String.valueOf(existing.getPrecio()));
        }
    }

    private static Producto cloneProducto(Producto original) {
        Producto copy = new Producto();
        copy.setId(original.getId());
        copy.setCodigo(original.getCodigo());
        copy.setNombre(original.getNombre());
        copy.setCategoria(original.getCategoria());
        copy.setProveedorCodigo(original.getProveedorCodigo());
        copy.setStock(original.getStock());
        copy.setCoste(original.getCoste());
        copy.setPrecio(original.getPrecio());
        copy.setActualizadoEn(original.getActualizadoEn());
        return copy;
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private double parseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
