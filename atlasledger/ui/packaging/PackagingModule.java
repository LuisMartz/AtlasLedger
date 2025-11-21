package atlasledger.ui.packaging;

import atlasledger.app.AppContext;
import atlasledger.simulation.TransportMode;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class PackagingModule extends BorderPane {

    private final ObservableList<TransportMode> modes;
    private final TableView<TransportMode> table = new TableView<>();

    public PackagingModule(AppContext context) {
        this.modes = FXCollections.observableArrayList(context.getSimulationService().getTransportModes());

        Label header = new Label("Empaquetado y transporte");
        header.getStyleClass().add("module-title");
        Label description = new Label("Define modos de transporte y costos para simular empaquetado y envios.");
        description.getStyleClass().add("module-description");
        VBox heading = new VBox(4, header, description);

        configureTable();

        TextField nameField = new TextField();
        nameField.setPromptText("Nombre");
        TextField costField = new TextField();
        costField.setPromptText("Costo por kg");
        TextField speedField = new TextField();
        speedField.setPromptText("Horas");
        TextField riskField = new TextField();
        riskField.setPromptText("Riesgo (%)");

        Button add = new Button("Agregar modo");
        add.getStyleClass().add("cta");
        add.setOnAction(e -> {
            try {
                TransportMode mode = new TransportMode(
                    nameField.getText().trim(),
                    Double.parseDouble(costField.getText()),
                    Double.parseDouble(speedField.getText()),
                    Double.parseDouble(riskField.getText()) / 100.0
                );
                context.getSimulationService().addTransportMode(mode);
                modes.setAll(context.getSimulationService().getTransportModes());
                nameField.clear();
                costField.clear();
                speedField.clear();
                riskField.clear();
            } catch (Exception ex) {
                // invalid input; ignore silently for now
            }
        });

        HBox form = new HBox(8, nameField, costField, speedField, riskField, add);
        form.getStyleClass().add("module-toolbar");
        HBox.setHgrow(nameField, Priority.SOMETIMES);
        HBox.setHgrow(costField, Priority.SOMETIMES);
        HBox.setHgrow(speedField, Priority.SOMETIMES);
        HBox.setHgrow(riskField, Priority.SOMETIMES);

        VBox wrapper = new VBox(12, heading, form, table);
        wrapper.getStyleClass().add("module-wrapper");
        VBox.setVgrow(table, Priority.ALWAYS);
        setPadding(new Insets(12));
        setCenter(wrapper);
    }

    private void configureTable() {
        TableColumn<TransportMode, String> nameCol = new TableColumn<>("Modo");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<TransportMode, Number> costCol = new TableColumn<>("Costo/kg");
        costCol.setCellValueFactory(new PropertyValueFactory<>("costPerKg"));

        TableColumn<TransportMode, Number> speedCol = new TableColumn<>("Horas");
        speedCol.setCellValueFactory(new PropertyValueFactory<>("speedHours"));

        TableColumn<TransportMode, Number> riskCol = new TableColumn<>("Riesgo");
        riskCol.setCellValueFactory(new PropertyValueFactory<>("riskFactor"));

        table.getColumns().setAll(nameCol, costCol, speedCol, riskCol);
        table.setItems(modes);
    }
}
