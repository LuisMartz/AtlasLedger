package atlasledger.ui.proveedores;

import atlasledger.model.Proveedor;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class ProviderDialog extends Dialog<Proveedor> {

    private final TextField codigoField = new TextField();
    private final TextField nombreField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField telefonoField = new TextField();
    private final TextField direccionField = new TextField();

    public ProviderDialog(Proveedor existing) {
        boolean editing = existing != null;
        setTitle(editing ? "Editar proveedor" : "Nuevo proveedor");
        setHeaderText(editing ? "Actualiza la informacion del proveedor." : "Introduce los datos del proveedor.");

        ButtonType saveType = new ButtonType(editing ? "Guardar" : "Crear", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = buildForm();
        populate(existing);
        getDialogPane().setContent(grid);

        getDialogPane().lookupButton(saveType).disableProperty().bind(
            codigoField.textProperty().isEmpty().or(nombreField.textProperty().isEmpty())
        );

        setResultConverter(button -> {
            if (button == saveType) {
                Proveedor proveedor = Optional.ofNullable(existing).map(ProviderDialog::cloneProveedor).orElseGet(Proveedor::new);
                proveedor.setCodigo(codigoField.getText().trim());
                proveedor.setNombre(nombreField.getText().trim());
                proveedor.setEmail(emailField.getText().trim());
                proveedor.setTelefono(telefonoField.getText().trim());
                proveedor.setDireccion(direccionField.getText().trim());
                return proveedor;
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

        grid.add(new Label("Nombre"), 0, row);
        grid.add(nombreField, 1, row++);

        grid.add(new Label("Email"), 0, row);
        grid.add(emailField, 1, row++);

        grid.add(new Label("Telefono"), 0, row);
        grid.add(telefonoField, 1, row++);

        grid.add(new Label("Direccion"), 0, row);
        grid.add(direccionField, 1, row);

        return grid;
    }

    private void populate(Proveedor existing) {
        if (existing != null) {
            codigoField.setText(existing.getCodigo());
            codigoField.setDisable(true);
            nombreField.setText(existing.getNombre());
            emailField.setText(Optional.ofNullable(existing.getEmail()).orElse(""));
            telefonoField.setText(Optional.ofNullable(existing.getTelefono()).orElse(""));
            direccionField.setText(Optional.ofNullable(existing.getDireccion()).orElse(""));
        }
    }

    private static Proveedor cloneProveedor(Proveedor proveedor) {
        Proveedor copy = new Proveedor();
        copy.setId(proveedor.getId());
        copy.setCodigo(proveedor.getCodigo());
        copy.setNombre(proveedor.getNombre());
        copy.setEmail(proveedor.getEmail());
        copy.setTelefono(proveedor.getTelefono());
        copy.setDireccion(proveedor.getDireccion());
        copy.setActualizadoEn(proveedor.getActualizadoEn());
        return copy;
    }
}
