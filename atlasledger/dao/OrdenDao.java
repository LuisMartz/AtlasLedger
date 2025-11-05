package atlasledger.dao;

import atlasledger.model.Orden;
import atlasledger.utils.DBHelper;
import atlasledger.utils.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class OrdenDao {

    private static final DateTimeFormatter SQLITE_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private OrdenDao() {
    }

    public static void guardar(Orden orden) {
        String sql = """
            INSERT INTO ordenes (codigo, fecha, proveedor_codigo, total, estado, updated_at)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(codigo) DO UPDATE SET
                fecha = excluded.fecha,
                proveedor_codigo = excluded.proveedor_codigo,
                total = excluded.total,
                estado = excluded.estado,
                updated_at = CURRENT_TIMESTAMP
        """;

        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orden.getCodigo());
            ps.setString(2, orden.getFecha() != null ? orden.getFecha().toString() : null);
            ps.setString(3, orden.getProveedorCodigo());
            ps.setDouble(4, orden.getTotal());
            ps.setString(5, orden.getEstado() != null ? orden.getEstado().name() : Orden.Estado.BORRADOR.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.error(OrdenDao.class, "Error guardando orden", e);
        }
    }

    public static List<Orden> listar() {
        List<Orden> list = new ArrayList<>();
        String sql = """
            SELECT id, codigo, fecha, proveedor_codigo, total, estado, updated_at
            FROM ordenes
            ORDER BY fecha DESC
        """;

        try (Connection conn = DBHelper.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            Logger.error(OrdenDao.class, "Error listando ordenes", e);
        }
        return list;
    }

    public static Optional<Orden> buscarPorCodigo(String codigo) {
        String sql = """
            SELECT id, codigo, fecha, proveedor_codigo, total, estado, updated_at
            FROM ordenes
            WHERE codigo = ?
        """;

        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            Logger.error(OrdenDao.class, "Error buscando orden", e);
        }
        return Optional.empty();
    }

    public static void eliminar(int id) {
        String sql = "DELETE FROM ordenes WHERE id = ?";
        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.error(OrdenDao.class, "Error eliminando orden", e);
        }
    }

    private static Orden mapRow(ResultSet rs) throws SQLException {
        Orden orden = new Orden();
        orden.setId(rs.getInt("id"));
        orden.setCodigo(rs.getString("codigo"));
        String fecha = rs.getString("fecha");
        if (fecha != null) {
            orden.setFecha(LocalDate.parse(fecha));
        }
        orden.setProveedorCodigo(rs.getString("proveedor_codigo"));
        orden.setTotal(rs.getDouble("total"));
        String estado = rs.getString("estado");
        if (estado != null) {
            orden.setEstado(Orden.Estado.valueOf(estado));
        }
        String updated = rs.getString("updated_at");
        if (updated != null) {
            orden.setActualizadoEn(LocalDateTime.parse(updated, SQLITE_DATE_TIME));
        }
        return orden;
    }
}
