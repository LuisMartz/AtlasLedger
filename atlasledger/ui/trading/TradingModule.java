package atlasledger.ui.trading;

import atlasledger.app.AppContext;
import atlasledger.model.Producto;
import atlasledger.service.SimulationService;
import atlasledger.simulation.PricePoint;
import java.text.NumberFormat;
import java.util.Locale;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class TradingModule extends BorderPane {

    private final AppContext context;
    private final SimulationService simulationService;
    private final ObservableList<Producto> productos;
    private final TableView<Producto> tableView = new TableView<>();
    private final LineChart<Number, Number> priceChart;
    private final XYChart.Series<Number, Number> priceSeries = new XYChart.Series<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    private Timeline ticker;

    public TradingModule(AppContext context) {
        this.context = context;
        this.simulationService = context.getSimulationService();
        this.productos = FXCollections.observableArrayList(context.getProductRepository().findAll());

        setPadding(new Insets(16));
        Label header = new Label("Trading / Variacion de precios");
        header.getStyleClass().add("module-title");
        Label description = new Label("Simula fluctuaciones de precios y observa su tendencia en el tiempo.");
        description.getStyleClass().add("module-description");
        VBox heading = new VBox(4, header, description);

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Ticks");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Precio");
        priceChart = new LineChart<>(xAxis, yAxis);
        priceChart.setLegendVisible(false);
        priceChart.getData().add(priceSeries);
        priceChart.setAnimated(false);

        configureTable();

        Button start = new Button("Iniciar simulacion");
        start.getStyleClass().add("cta");
        start.setOnAction(e -> startSimulation());

        Button stop = new Button("Detener");
        stop.setOnAction(e -> stopSimulation());

        Button refresh = new Button("Refrescar productos");
        refresh.setOnAction(e -> reloadProducts(context));

        HBox toolbar = new HBox(8, start, stop, refresh);
        toolbar.getStyleClass().add("module-toolbar");

        VBox right = new VBox(10, priceChart);
        VBox.setVgrow(priceChart, Priority.ALWAYS);

        HBox content = new HBox(12, tableView, right);
        HBox.setHgrow(right, Priority.ALWAYS);
        HBox.setHgrow(tableView, Priority.SOMETIMES);

        VBox wrapper = new VBox(12, heading, toolbar, content);
        wrapper.getStyleClass().add("module-wrapper");
        VBox.setVgrow(content, Priority.ALWAYS);
        setCenter(wrapper);
    }

    private void configureTable() {
        TableColumn<Producto, String> codeCol = new TableColumn<>("Codigo");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        TableColumn<Producto, String> nameCol = new TableColumn<>("Nombre");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<Producto, Number> priceCol = new TableColumn<>("Precio sim.");
        priceCol.setCellValueFactory(cell -> new SimpleDoubleProperty(
            simulationService.getSimulatedPrice(cell.getValue().getCodigo())));

        tableView.getColumns().setAll(codeCol, nameCol, priceCol);
        tableView.setItems(productos);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            priceSeries.getData().clear();
            if (sel != null) {
                loadHistory(sel.getCodigo());
            }
        });
    }

    private void startSimulation() {
        stopSimulation();
        ticker = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
            simulationService.simulatePriceStep();
            productos.setAll(context.getProductRepository().findAll());
            Producto sel = tableView.getSelectionModel().getSelectedItem();
            if (sel != null) {
                loadHistory(sel.getCodigo());
            }
        }));
        ticker.setCycleCount(Timeline.INDEFINITE);
        ticker.play();
    }

    private void stopSimulation() {
        if (ticker != null) {
            ticker.stop();
        }
    }

    private void reloadProducts(AppContext context) {
        productos.setAll(context.getProductRepository().findAll());
    }

    private void loadHistory(String productCode) {
        priceSeries.getData().clear();
        var points = simulationService.getPriceHistoryFor(productCode, 50);
        int index = 0;
        for (PricePoint point : points) {
            priceSeries.getData().add(new XYChart.Data<>(index++, point.getPrice()));
        }
    }
}
