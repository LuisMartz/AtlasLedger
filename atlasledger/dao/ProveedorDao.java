package atlasledger.dao;

import atlasledger.model.Proveedor;
import atlasledger.utils.DBHelper;
import atlasledger.utils.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ProveedorDao {

    private static final DateTimeFormatter SQLITE_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ProveedorDao() {
    }

    public static void guardar(Proveedor proveedor) {
        String sql = """
            INSERT INTO proveedores (codigo, nombre, email, telefono, direccion, updated_at)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(codigo) DO UPDATE SET
                nombre = excluded.nombre,
                email = excluded.email,
                telefono = excluded.telefono,
                direccion = excluded.direccion,
                updated_at = CURRENT_TIMESTAMP
        """;

        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, proveedor.getCodigo());
            ps.setString(2, proveedor.getNombre());
            ps.setString(3, proveedor.getEmail());
            ps.setString(4, proveedor.getTelefono());
            ps.setString(5, proveedor.getDireccion());
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.error(ProveedorDao.class, "Error guardando proveedor", e);
        }
    }

    public static List<Proveedor> listar() {
        List<Proveedor> list = new ArrayList<>();
        String sql = """
            SELECT id, codigo, nombre, email, telefono, direccion, updated_at
            FROM proveedores
            ORDER BY nombre ASC
        """;

        try (Connection conn = DBHelper.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            Logger.error(ProveedorDao.class, "Error listando proveedores", e);
        }
        return list;
    }

    public static Optional<Proveedor> buscarPorCodigo(String codigo) {
        String sql = """
            SELECT id, codigo, nombre, email, telefono, direccion, updated_at
            FROM proveedores
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
            Logger.error(ProveedorDao.class, "Error buscando proveedor", e);
        }
        return Optional.empty();
    }

    public static void eliminar(int id) {
        String sql = "DELETE FROM proveedores WHERE id = ?";
        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.error(ProveedorDao.class, "Error eliminando proveedor", e);
        }
    }

    private static Proveedor mapRow(ResultSet rs) throws SQLException {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(rs.getInt("id"));
        proveedor.setCodigo(rs.getString("codigo"));
        proveedor.setNombre(rs.getString("nombre"));
        proveedor.setEmail(rs.getString("email"));
        proveedor.setTelefono(rs.getString("telefono"));
        proveedor.setDireccion(rs.getString("direccion"));
        String updated = rs.getString("updated_at");
        if (updated != null) {
            proveedor.setActualizadoEn(LocalDateTime.parse(updated, SQLITE_DATE_TIME));
        }
        return proveedor;
    }
}
