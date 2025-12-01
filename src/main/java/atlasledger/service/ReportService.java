package atlasledger.service;

import atlasledger.model.Informe;
import atlasledger.model.Orden;
import atlasledger.model.Producto;
import atlasledger.repository.OrderRepository;
import atlasledger.repository.ProductRepository;
import atlasledger.utils.DBHelper;
import atlasledger.utils.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ReportService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public ReportSnapshot generarResumenInventario() {
        List<Producto> productos = productRepository.findAll();

        double valorInventario = productos.stream().mapToDouble(p -> p.getPrecio() * p.getStock()).sum();
        double saldoInventario = productos.stream().mapToDouble(p -> p.getCoste() * p.getStock()).sum();
        double margenPromedio = productos.isEmpty()
            ? 0.0
            : productos.stream().mapToDouble(Producto::getMargen).average().orElse(0.0);

        Map<String, Number> resumen = new LinkedHashMap<>();
        resumen.put("Total de productos", productos.size());
        resumen.put("Valor inventario", valorInventario);
        resumen.put("Saldo inventario", saldoInventario);
        resumen.put("Margen promedio", margenPromedio);

        Map<String, Long> categorias = productos.stream()
            .collect(Collectors.groupingBy(p -> {
                String categoria = p.getCategoria();
                return categoria == null || categoria.isBlank() ? "Sin categoria" : categoria;
            }, Collectors.counting()));

        Map<String, Number> categoriasOrdenadas = categorias.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (a, b) -> a,
                LinkedHashMap::new
            ));

        Informe informe = new Informe("Resumen Inventario", Informe.Tipo.INVENTARIO,
            toJson(resumen, categoriasOrdenadas, "Categorias"));
        informe.setGeneradoEn(LocalDateTime.now());
        persistirInforme(informe);

        return new ReportSnapshot(informe, resumen, categoriasOrdenadas, "Participacion por categoria");
    }

    public ReportSnapshot generarComprasPorProveedor() {
        List<Orden> ordenes = orderRepository.findAll();

        double totalComprado = ordenes.stream().mapToDouble(Orden::getTotal).sum();
        double promedio = ordenes.isEmpty() ? 0.0 : totalComprado / ordenes.size();

        Map<String, Number> resumen = new LinkedHashMap<>();
        resumen.put("Total de ordenes", ordenes.size());
        resumen.put("Total comprado", totalComprado);
        resumen.put("Orden promedio", promedio);

        Map<String, Double> porProveedor = ordenes.stream()
            .collect(Collectors.groupingBy(orden -> {
                String proveedor = orden.getProveedorCodigo();
                return proveedor == null || proveedor.isBlank() ? "Sin proveedor" : proveedor;
            }, Collectors.summingDouble(Orden::getTotal)));

        Map<String, Number> proveedoresOrdenados = porProveedor.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue(),
                (a, b) -> a,
                LinkedHashMap::new
            ));

        Informe informe = new Informe("Compras por Proveedor", Informe.Tipo.COMPRAS,
            toJson(resumen, proveedoresOrdenados, "Proveedores"));
        informe.setGeneradoEn(LocalDateTime.now());
        persistirInforme(informe);

        return new ReportSnapshot(informe, resumen, proveedoresOrdenados, "Distribucion por proveedor");
    }

    private String toJson(Map<String, Number> summary,
                          Map<String, Number> breakdown,
                          String breakdownLabel) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"summary\":{");
        appendMap(sb, summary);
        sb.append("},\"breakdownLabel\":\"").append(escape(breakdownLabel)).append("\",\"breakdown\":{");
        appendMap(sb, breakdown);
        sb.append("}}");
        return sb.toString();
    }

    private void appendMap(StringBuilder sb, Map<String, Number> map) {
        boolean first = true;
        for (Map.Entry<String, Number> entry : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(escape(entry.getKey())).append('"').append(':')
                .append(formatNumber(entry.getValue()));
        }
    }

    private String formatNumber(Number number) {
        if (number == null) {
            return "0";
        }
        if (number instanceof Integer || number instanceof Long) {
            return String.valueOf(number.longValue());
        }
        return String.format(Locale.US, "%.2f", number.doubleValue());
    }

    private String escape(String value) {
        return value.replace("\"", "\\\"");
    }

    private void persistirInforme(Informe informe) {
        String sql = """
            INSERT INTO informes (nombre, tipo, definicion_json, generado_en)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(nombre) DO UPDATE SET
                tipo = excluded.tipo,
                definicion_json = excluded.definicion_json,
                generado_en = excluded.generado_en
        """;

        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, informe.getNombre());
            ps.setString(2, informe.getTipo().name());
            ps.setString(3, informe.getDefinicionJson());
            ps.setString(4, informe.getGeneradoEn() != null ? informe.getGeneradoEn().toString() : null);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.error(ReportService.class, "Error guardando informe", e);
        }
    }
}
