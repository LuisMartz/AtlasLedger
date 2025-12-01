package atlasledger.ui.dashboard;

import atlasledger.app.AppContext;
import atlasledger.model.AppLog;
import atlasledger.service.AnalyticsService;
import atlasledger.service.DocumentService;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AnalyticsModule extends VBox {

    private final AnalyticsService analyticsService;
    private final DocumentService documentService;
    private final PieChart inventoryChart = new PieChart();
    private final BarChart<String, Number> stockChart;
    private final LineChart<String, Number> ordersChart;
    private final BarChart<String, Number> logsChart;
    private final TableView<AppLog> logsTable = new TableView<>();
    private final Label documentsSummary = new Label();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

    public AnalyticsModule(AppContext context) {
        this.analyticsService = context.getAnalyticsService();
        this.documentService = context.getDocumentService();
        getStyleClass().add("module-wrapper");
        setSpacing(18);

        Label header = new Label("Panel de analitica");
        header.getStyleClass().add("module-title");

        Label description = new Label("Visualiza tendencias de inventario, compras y eventos operativos.");
        description.getStyleClass().add("module-description");

        VBox heading = new VBox(4, header, description);
        heading.setAlignment(Pos.CENTER_LEFT);

        CategoryAxis stockXAxis = new CategoryAxis();
        NumberAxis stockYAxis = new NumberAxis();
        stockXAxis.setLabel("Proveedor");
        stockYAxis.setLabel("Stock");
        stockChart = new BarChart<>(stockXAxis, stockYAxis);
        stockChart.setLegendVisible(false);
        stockChart.setTitle("Stock por proveedor");

        CategoryAxis ordersXAxis = new CategoryAxis();
        NumberAxis ordersYAxis = new NumberAxis();
        ordersXAxis.setLabel("Periodo");
        ordersYAxis.setLabel("Total");
        ordersChart = new LineChart<>(ordersXAxis, ordersYAxis);
        ordersChart.setLegendVisible(false);
        ordersChart.setTitle("Compras por mes");

        CategoryAxis logsXAxis = new CategoryAxis();
        NumberAxis logsYAxis = new NumberAxis();
        logsXAxis.setLabel("Nivel");
        logsYAxis.setLabel("Eventos");
        logsChart = new BarChart<>(logsXAxis, logsYAxis);
        logsChart.setLegendVisible(false);
        logsChart.setTitle("Eventos registrados por nivel");

        configureCharts();
        configureLogsTable();

        GridPane grid = new GridPane();
        grid.getStyleClass().add("analytics-grid");
        grid.add(wrapChart(inventoryChart, "Inventario por categoria"), 0, 0);
        grid.add(wrapChart(stockChart, "Stock agrupado por proveedor"), 1, 0);
        grid.add(wrapChart(ordersChart, "Evolucion mensual de compras"), 0, 1);
        grid.add(wrapChart(logsChart, "Eventos del sistema"), 1, 1);

        Button refresh = new Button("Actualizar analitica");
        refresh.setOnAction(event -> refresh());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(8, spacer, refresh);
        toolbar.getStyleClass().add("module-toolbar");
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        Label logsHeader = new Label("Actividad reciente");
        logsHeader.getStyleClass().add("module-title");

        documentsSummary.getStyleClass().add("module-description");
        VBox logsBox = new VBox(8, logsHeader, documentsSummary, logsTable);
        VBox.setVgrow(logsTable, Priority.ALWAYS);

        getChildren().addAll(heading, grid, toolbar, logsBox);

        refresh();
    }

    private void configureCharts() {
        inventoryChart.setLegendVisible(false);
        inventoryChart.setLabelsVisible(true);
        inventoryChart.setClockwise(true);
    }

    private VBox wrapChart(javafx.scene.Node chart, String title) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-card-label");
        VBox box = new VBox(8, titleLabel, chart);
        box.getStyleClass().add("chart-card");
        VBox.setVgrow(chart, Priority.ALWAYS);
        return box;
    }

    private void configureLogsTable() {
        TableColumn<AppLog, String> levelColumn = new TableColumn<>("Nivel");
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        levelColumn.setPrefWidth(90);

        TableColumn<AppLog, String> sourceColumn = new TableColumn<>("Origen");
        sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
        sourceColumn.setPrefWidth(140);

        TableColumn<AppLog, String> timeColumn = new TableColumn<>("Momento");
        timeColumn.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getCreatedAt() != null ? cell.getValue().getCreatedAt().toString().replace("T", " ") : ""));
        timeColumn.setPrefWidth(160);

        TableColumn<AppLog, String> messageColumn = new TableColumn<>("Mensaje");
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        messageColumn.setPrefWidth(420);

        logsTable.getColumns().setAll(levelColumn, sourceColumn, timeColumn, messageColumn);
        logsTable.setPlaceholder(new Label("No hay actividad registrada en esta sesion."));
        logsTable.setPrefHeight(220);
    }

    private void refresh() {
        populateInventoryChart();
        populateStockChart();
        populateOrdersChart();
        populateLogsChart();
        populateLogsTable();
        updateDocumentsSummary();
    }

    private void populateInventoryChart() {
        ObservableList<PieChart.Data> dataPoints = FXCollections.observableArrayList();
        Map<String, Double> data = analyticsService.inventoryValueByCategory();
        data.forEach((categoria, total) -> dataPoints.add(new PieChart.Data(
            categoria + " (" + currencyFormat.format(total) + ")", total)));
        if (dataPoints.isEmpty()) {
            dataPoints.add(new PieChart.Data("Sin datos", 1));
        }
        inventoryChart.setData(dataPoints);
    }

    private void populateStockChart() {
        Map<String, Integer> data = analyticsService.stockBySupplier();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        data.forEach((proveedor, stock) -> series.getData().add(new XYChart.Data<>(proveedor, stock)));
        if (series.getData().isEmpty()) {
            series.getData().add(new XYChart.Data<>("Sin datos", 0));
        }
        stockChart.getData().setAll(series);
    }

    private void populateOrdersChart() {
        Map<String, Double> data = analyticsService.ordersByMonth(12);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        data.forEach((mes, total) -> series.getData().add(new XYChart.Data<>(mes, total)));
        if (series.getData().isEmpty()) {
            series.getData().add(new XYChart.Data<>("Sin datos", 0));
        }
        ordersChart.getData().setAll(series);
    }

    private void populateLogsChart() {
        Map<String, Long> counts = analyticsService.logCountByLevel();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        if (counts.isEmpty()) {
            counts = new LinkedHashMap<>();
            counts.put("INFO", 0L);
            counts.put("WARN", 0L);
            counts.put("ERROR", 0L);
        }
        counts.forEach((level, total) -> series.getData().add(new XYChart.Data<>(level, total)));
        logsChart.getData().setAll(series);
    }

    private void populateLogsTable() {
        List<AppLog> recent = analyticsService.recentLogs(50);
        logsTable.getItems().setAll(recent);
    }

    private void updateDocumentsSummary() {
        int pending = documentService.pendingDocuments().size();
        documentsSummary.setText("Documentos pendientes por subir: " + pending);
    }
}
