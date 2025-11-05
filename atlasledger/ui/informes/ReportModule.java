package atlasledger.ui.informes;

import atlasledger.app.AppContext;
import atlasledger.model.Informe;
import atlasledger.service.ReportService;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ReportModule extends BorderPane {

    private final ReportService reportService;
    private final TextArea output;
    private final Label reportTitle = new Label("Selecciona un informe para visualizar.");

    public ReportModule(AppContext context) {
        this.reportService = context.getReportService();

        Label header = new Label("Centro de informes");
        header.getStyleClass().add("module-title");

        Label description = new Label("Genera reportes ejecutivos listos para compartir con tu equipo.");
        description.getStyleClass().add("module-description");

        VBox headerBox = new VBox(4, header, description);

        output = new TextArea();
        output.setEditable(false);
        output.setWrapText(true);
        output.setPrefRowCount(16);
        output.getStyleClass().add("report-output");

        Button inventario = new Button("Resumen de inventario");
        inventario.getStyleClass().add("cta");
        inventario.setOnAction(event -> mostrar(reportService.generarResumenInventario()));

        Button compras = new Button("Compras por proveedor");
        compras.setOnAction(event -> mostrar(reportService.generarComprasPorProveedor()));

        Button exportButton = new Button("Copiar contenido");
        exportButton.setOnAction(event -> exportar());

        HBox actions = new HBox(10, inventario, compras, exportButton);
        actions.getStyleClass().add("module-toolbar");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox wrapper = new VBox(18, headerBox, actions, reportTitle, output, spacer);
        wrapper.getStyleClass().add("module-wrapper");
        VBox.setVgrow(output, Priority.ALWAYS);

        setCenter(wrapper);
    }

    private void mostrar(Informe informe) {
        reportTitle.setText(informe.getNombre() + " - " + informe.getTipo());
        output.setText("""
            Informe: %s
            Generado el: %s
            Tipo: %s

            %s
        """.formatted(
            informe.getNombre(),
            informe.getGeneradoEn(),
            informe.getTipo(),
            informe.getDefinicionJson()
        ));
    }

    private void exportar() {
        if (output.getText().isBlank()) {
            return;
        }
        output.selectAll();
        output.copy();
        output.positionCaret(output.getText().length());
    }
}
