package atlasledger.dao;

import atlasledger.model.Producto;
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

public final class ProductoDao {

    private static final DateTimeFormatter SQLITE_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ProductoDao() {
    }

    public static void guardar(Producto producto) {
        String sql = """
            INSERT INTO productos (codigo, nombre, categoria, proveedor_codigo, stock, coste, precio, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(codigo) DO UPDATE SET
                nombre = excluded.nombre,
                categoria = excluded.categoria,
                proveedor_codigo = excluded.proveedor_codigo,
                stock = excluded.stock,
                coste = excluded.coste,
                precio = excluded.precio,
                updated_at = CURRENT_TIMESTAMP
        """;

        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setString(3, producto.getCategoria());
            ps.setString(4, producto.getProveedorCodigo());
            ps.setInt(5, producto.getStock());
            ps.setDouble(6, producto.getCoste());
            ps.setDouble(7, producto.getPrecio());
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.error(ProductoDao.class, "Error guardando producto", e);
        }
    }

    public static List<Producto> listar() {
        List<Producto> list = new ArrayList<>();
        String sql = """
            SELECT id, codigo, nombre, categoria, proveedor_codigo, stock, coste, precio, updated_at
            FROM productos
            ORDER BY nombre ASC
        """;

        try (Connection conn = DBHelper.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            Logger.error(ProductoDao.class, "Error listando productos", e);
        }
        return list;
    }

    public static Optional<Producto> buscarPorCodigo(String codigo) {
        String sql = """
            SELECT id, codigo, nombre, categoria, proveedor_codigo, stock, coste, precio, updated_at
            FROM productos
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
            Logger.error(ProductoDao.class, "Error buscando producto", e);
        }
        return Optional.empty();
    }

    public static void eliminar(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";
        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.error(ProductoDao.class, "Error eliminando producto", e);
        }
    }

    private static Producto mapRow(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setId(rs.getInt("id"));
        producto.setCodigo(rs.getString("codigo"));
        producto.setNombre(rs.getString("nombre"));
        producto.setCategoria(rs.getString("categoria"));
        producto.setProveedorCodigo(rs.getString("proveedor_codigo"));
        producto.setStock(rs.getInt("stock"));
        producto.setCoste(rs.getDouble("coste"));
        producto.setPrecio(rs.getDouble("precio"));
        String updated = rs.getString("updated_at");
        if (updated != null) {
            producto.setActualizadoEn(LocalDateTime.parse(updated, SQLITE_DATE_TIME));
        }
        return producto;
    }
}
