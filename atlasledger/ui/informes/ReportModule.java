package atlasledger.ui.informes;

import atlasledger.app.AppContext;
import atlasledger.model.Informe;
import atlasledger.service.ReportService;
import atlasledger.service.ReportSnapshot;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ReportModule extends BorderPane {

    private final ReportService reportService;
    private final PieChart breakdownChart = new PieChart();
    private final FlowPane summaryPane = new FlowPane(12, 12);
    private final Label breakdownTitle = new Label();
    private final Label reportTitle = new Label("Selecciona un informe para visualizar.");
    private final Label statusLabel = new Label("Genera un reporte para ver detalles.");
    private final ProgressIndicator loader = new ProgressIndicator();
    private final TextArea output;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("es", "ES"));
    private ReportSnapshot currentSnapshot;

    public ReportModule(AppContext context) {
        this.reportService = context.getReportService();

        Label header = new Label("Centro de informes");
        header.getStyleClass().add("module-title");

        Label description = new Label("Genera reportes ejecutivos listos para compartir con tu equipo.");
        description.getStyleClass().add("module-description");

        VBox headerBox = new VBox(4, header, description);

        Button inventario = new Button("Resumen de inventario");
        inventario.getStyleClass().add("cta");
        inventario.setOnAction(event -> runReport(reportService::generarResumenInventario));

        Button compras = new Button("Compras por proveedor");
        compras.setOnAction(event -> runReport(reportService::generarComprasPorProveedor));

        Button exportButton = new Button("Copiar contenido");
        exportButton.setOnAction(event -> exportar());

        HBox actions = new HBox(10, inventario, compras, exportButton);
        actions.getStyleClass().add("module-toolbar");

        loader.setVisible(false);
        loader.setPrefSize(18, 18);
        loader.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        statusLabel.getStyleClass().add("status-label");
        HBox statusRow = new HBox(8, statusLabel, loader);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        summaryPane.getStyleClass().add("stats-bar");
        summaryPane.setPrefWrapLength(600);

        breakdownChart.setLegendVisible(false);
        breakdownChart.setLabelsVisible(true);

        breakdownTitle.getStyleClass().add("module-description");
        VBox breakdownBox = new VBox(6, breakdownTitle, breakdownChart);
        breakdownBox.getStyleClass().add("chart-card");

        reportTitle.getStyleClass().add("report-title");

        output = new TextArea();
        output.setEditable(false);
        output.setWrapText(true);
        output.setPrefRowCount(16);
        output.getStyleClass().add("report-output");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox wrapper = new VBox(18,
            headerBox,
            actions,
            statusRow,
            summaryPane,
            breakdownBox,
            reportTitle,
            output,
            spacer
        );
        wrapper.getStyleClass().add("module-wrapper");
        VBox.setVgrow(output, Priority.ALWAYS);

        setCenter(wrapper);
    }

    private void runReport(Supplier<ReportSnapshot> supplier) {
        Task<ReportSnapshot> task = new Task<>() {
            @Override
            protected ReportSnapshot call() {
                return supplier.get();
            }
        };
        task.setOnRunning(event -> showLoading("Generando reporte..."));
        task.setOnSucceeded(event -> {
            hideLoading("Reporte generado correctamente.");
            displaySnapshot(task.getValue());
        });
        task.setOnFailed(event -> {
            hideLoading("No se pudo generar el reporte.");
            if (!statusLabel.getStyleClass().contains("error")) {
                statusLabel.getStyleClass().add("error");
            }
        });
        Thread worker = new Thread(task, "report-generator");
        worker.setDaemon(true);
        worker.start();
    }

    private void displaySnapshot(ReportSnapshot snapshot) {
        currentSnapshot = snapshot;
        statusLabel.getStyleClass().remove("error");
        Informe informe = snapshot.getInforme();
        reportTitle.setText(informe.getNombre() + " - " + informe.getTipo());
        statusLabel.setText("Ultimo reporte generado: " + formatDateTime(informe.getGeneradoEn()));
        updateSummary(snapshot.getSummary());
        updateBreakdown(snapshot.getBreakdown(), snapshot.getBreakdownTitle());
        output.setText(prettyPrintJson(informe.getDefinicionJson()));
    }

    private void updateSummary(Map<String, Number> summary) {
        summaryPane.getChildren().clear();
        if (summary.isEmpty()) {
            return;
        }
        summary.forEach((label, value) -> summaryPane.getChildren().add(createCard(label, value)));
    }

    private VBox createCard(String label, Number value) {
        Label title = new Label(label);
        title.getStyleClass().add("stat-card-label");

        Label amount = new Label(formatNumber(label, value));
        amount.getStyleClass().add("stat-card-value");

        VBox card = new VBox(4, title, amount);
        card.getStyleClass().add("stat-card");
        return card;
    }

    private void updateBreakdown(Map<String, Number> breakdown, String title) {
        breakdownTitle.setText(title);
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        breakdown.forEach((key, value) -> pieData.add(new PieChart.Data(key, value.doubleValue())));
        if (pieData.isEmpty()) {
            pieData.add(new PieChart.Data("Sin datos", 1));
        }
        breakdownChart.setData(pieData);
    }

    private void exportar() {
        if (currentSnapshot == null || output.getText().isBlank()) {
            return;
        }
        output.selectAll();
        output.copy();
        output.positionCaret(output.getText().length());
        statusLabel.setText("Contenido copiado al portapapeles.");
    }

    private void showLoading(String message) {
        statusLabel.getStyleClass().remove("error");
        statusLabel.setText(message);
        loader.setVisible(true);
    }

    private void hideLoading(String message) {
        loader.setVisible(false);
        statusLabel.setText(message);
    }

    private String formatNumber(String label, Number value) {
        if (value == null) {
            return "0";
        }
        if (value instanceof Integer || value instanceof Long) {
            return numberFormat.format(value.longValue());
        }
        String lower = label.toLowerCase(Locale.getDefault());
        if (lower.contains("valor") || lower.contains("saldo") || lower.contains("total") || lower.contains("promedio") || lower.contains("orden")) {
            return currencyFormat.format(value.doubleValue());
        }
        return numberFormat.format(value.doubleValue());
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "N/D" : dateTime.toString().replace('T', ' ');
    }

    private String prettyPrintJson(String json) {
        if (json == null || json.isBlank()) {
            return "";
        }
        StringBuilder pretty = new StringBuilder();
        int indent = 0;
        boolean inQuotes = false;
        for (int i = 0; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (ch == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
                pretty.append(ch);
                continue;
            }
            if (!inQuotes) {
                switch (ch) {
                    case '{':
                    case '[':
                        pretty.append(ch).append('\n');
                        indent++;
                        appendIndent(pretty, indent);
                        continue;
                    case '}':
                    case ']':
                        pretty.append('\n');
                        indent = Math.max(0, indent - 1);
                        appendIndent(pretty, indent);
                        pretty.append(ch);
                        continue;
                    case ',':
                        pretty.append(ch).append('\n');
                        appendIndent(pretty, indent);
                        continue;
                    case ':':
                        pretty.append(": ");
                        continue;
                    default:
                        if (Character.isWhitespace(ch)) {
                            continue;
                        }
                }
            }
            pretty.append(ch);
        }
        return pretty.toString();
    }

    private void appendIndent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
    }
}
