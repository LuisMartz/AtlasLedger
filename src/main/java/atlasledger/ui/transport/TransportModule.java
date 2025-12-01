package atlasledger.ui.transport;

import atlasledger.app.AppContext;
import atlasledger.simulation.TransportEvent;
import java.time.LocalDateTime;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class TransportModule extends BorderPane {

    private final AppContext context;
    private final TableView<TransportEvent> table = new TableView<>();

    public TransportModule(AppContext context) {
        this.context = context;

        Label header = new Label("Simulacion de transporte");
        header.getStyleClass().add("module-title");
        Label description = new Label("Genera recorridos simulados para observar fases de envio.");
        description.getStyleClass().add("module-description");
        VBox heading = new VBox(4, header, description);

        configureTable();

        Button simulate = new Button("Simular viaje");
        simulate.getStyleClass().add("cta");
        simulate.setOnAction(e -> runSimulation());

        HBox toolbar = new HBox(8, simulate);
        toolbar.getStyleClass().add("module-toolbar");

        VBox wrapper = new VBox(12, heading, toolbar, table);
        wrapper.getStyleClass().add("module-wrapper");
        VBox.setVgrow(table, Priority.ALWAYS);

        setPadding(new Insets(12));
        setCenter(wrapper);
    }

    private void configureTable() {
        TableColumn<TransportEvent, String> idCol = new TableColumn<>("Envio");
        idCol.setCellValueFactory(new PropertyValueFactory<>("shipmentId"));

        TableColumn<TransportEvent, String> originCol = new TableColumn<>("Origen");
        originCol.setCellValueFactory(new PropertyValueFactory<>("origin"));

        TableColumn<TransportEvent, String> destCol = new TableColumn<>("Destino");
        destCol.setCellValueFactory(new PropertyValueFactory<>("destination"));

        TableColumn<TransportEvent, String> statusCol = new TableColumn<>("Estado");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<TransportEvent, LocalDateTime> etaCol = new TableColumn<>("ETA");
        etaCol.setCellValueFactory(new PropertyValueFactory<>("eta"));

        table.getColumns().setAll(idCol, originCol, destCol, statusCol, etaCol);
    }

    private void runSimulation() {
        List<TransportEvent> events = context.getSimulationService().simulateTransportJourney();
        table.getItems().setAll(events);
    }
}
