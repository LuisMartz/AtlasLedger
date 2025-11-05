package atlasledger.ui.informes;

import atlasledger.app.AppContext;
import atlasledger.model.Informe;
import atlasledger.service.ReportService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ReportModule extends BorderPane {

    private final ReportService reportService;
    private final TextArea output;

    public ReportModule(AppContext context) {
        this.reportService = context.getReportService();

        setPadding(new Insets(16));

        Label header = new Label("Centro de Informes");
        header.getStyleClass().add("module-title");

        output = new TextArea();
        output.setEditable(false);
        output.setPrefRowCount(20);
        output.setStyle("-fx-font-family: 'Consolas';");

        Button inventario = new Button("Resumen Inventario");
        inventario.setOnAction(event -> mostrar(reportService.generarResumenInventario()));

        Button compras = new Button("Compras por Proveedor");
        compras.setOnAction(event -> mostrar(reportService.generarComprasPorProveedor()));

        HBox acciones = new HBox(8, inventario, compras);
        acciones.setPadding(new Insets(0, 0, 12, 0));

        VBox top = new VBox(header, acciones);
        top.setSpacing(8);
        setTop(top);
        setCenter(output);
    }

    private void mostrar(Informe informe) {
        output.setText("""
            Informe: %s
            Fecha: %s
            Tipo: %s

            %s
        """.formatted(
            informe.getNombre(),
            informe.getGeneradoEn(),
            informe.getTipo(),
            informe.getDefinicionJson()
        ));
    }
}
