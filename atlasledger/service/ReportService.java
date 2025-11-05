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
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ReportService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public Informe generarResumenInventario() {
        List<Producto> productos = productRepository.findAll();
        DoubleSummaryStatistics statsValor = productos.stream()
            .mapToDouble(p -> p.getPrecio() * p.getStock())
            .summaryStatistics();

        Map<String, Long> productosPorCategoria = productos.stream()
            .collect(Collectors.groupingBy(Producto::getCategoria, Collectors.counting()));

        String payload = """
            {
              "totalProductos": %d,
              "valorInventario": %.2f,
              "saldoInventario": %.2f,
              "categorias": %s
            }
        """.formatted(
            productos.size(),
            statsValor.getSum(),
            productos.stream().mapToDouble(p -> p.getCoste() * p.getStock()).sum(),
            productosPorCategoria
        );

        Informe informe = new Informe("Resumen Inventario", Informe.Tipo.INVENTARIO, payload);
        informe.setGeneradoEn(LocalDateTime.now());
        persistirInforme(informe);
        return informe;
    }

    public Informe generarComprasPorProveedor() {
        List<Orden> ordenes = orderRepository.findAll();

        Map<String, Double> totales = ordenes.stream()
            .collect(Collectors.groupingBy(Orden::getProveedorCodigo, Collectors.summingDouble(Orden::getTotal)));

        String payload = """
            {
              "totalOrdenes": %d,
              "totalComprado": %.2f,
              "porProveedor": %s
            }
        """.formatted(
            ordenes.size(),
            ordenes.stream().mapToDouble(Orden::getTotal).sum(),
            totales
        );

        Informe informe = new Informe("Compras por Proveedor", Informe.Tipo.COMPRAS, payload);
        informe.setGeneradoEn(LocalDateTime.now());
        persistirInforme(informe);
        return informe;
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
