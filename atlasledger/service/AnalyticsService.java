package atlasledger.service;

import atlasledger.model.AppLog;
import atlasledger.utils.DBHelper;
import atlasledger.utils.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AnalyticsService {

    public Map<String, Double> inventoryValueByCategory() {
        String sql = """
            SELECT COALESCE(categoria, 'Sin categoria') AS categoria,
                   SUM(precio * stock) AS total
            FROM productos
            GROUP BY categoria
            ORDER BY total DESC
        """;
        Map<String, Double> result = new LinkedHashMap<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("categoria"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            Logger.error(AnalyticsService.class, "Error obteniendo valor de inventario por categoria", e);
        }
        return result;
    }

    public Map<String, Integer> stockBySupplier() {
        String sql = """
            SELECT COALESCE(proveedor_codigo, 'Sin proveedor') AS proveedor,
                   SUM(stock) AS total
            FROM productos
            GROUP BY proveedor_codigo
            ORDER BY total DESC
        """;
        Map<String, Integer> result = new LinkedHashMap<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("proveedor"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            Logger.error(AnalyticsService.class, "Error obteniendo stock por proveedor", e);
        }
        return result;
    }

    public Map<String, Double> ordersByMonth(int months) {
        String sql = """
            SELECT strftime('%Y-%m', fecha) AS periodo,
                   SUM(total) AS total
            FROM ordenes
            WHERE fecha IS NOT NULL
            GROUP BY strftime('%Y-%m', fecha)
            ORDER BY periodo DESC
            LIMIT ?
        """;
        Map<String, Double> reversed = new LinkedHashMap<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, months);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reversed.put(rs.getString("periodo"), rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            Logger.error(AnalyticsService.class, "Error obteniendo ordenes por mes", e);
        }
        TreeMap<String, Double> sorted = new TreeMap<>(reversed);
        Map<String, Double> chronologicallyOrdered = new LinkedHashMap<>();
        sorted.forEach(chronologicallyOrdered::put);
        return chronologicallyOrdered;
    }

    public Map<String, Long> logCountByLevel() {
        String sql = """
            SELECT level, COUNT(*) AS total
            FROM app_logs
            GROUP BY level
        """;
        Map<String, Long> result = new LinkedHashMap<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("level"), rs.getLong("total"));
            }
        } catch (SQLException e) {
            Logger.error(AnalyticsService.class, "Error obteniendo resumen de logs", e);
        }
        return result;
    }

    public List<AppLog> recentLogs(int limit) {
        String sql = """
            SELECT id, level, source, message, created_at
            FROM app_logs
            ORDER BY id DESC
            LIMIT ?
        """;
        List<AppLog> logs = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AppLog log = new AppLog();
                    log.setId(rs.getInt("id"));
                    log.setLevel(rs.getString("level"));
                    log.setSource(rs.getString("source"));
                    log.setMessage(rs.getString("message"));
                    String created = rs.getString("created_at");
                    if (created != null) {
                        log.setCreatedAt(LocalDateTime.parse(created.replace(" ", "T")));
                    }
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            Logger.error(AnalyticsService.class, "Error obteniendo logs recientes", e);
        }
        return logs;
    }
}
