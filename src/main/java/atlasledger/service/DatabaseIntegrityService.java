package atlasledger.service;

import atlasledger.utils.DBHelper;
import atlasledger.utils.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseIntegrityService {

    public List<String> validarReferencias() {
        List<String> issues = new ArrayList<>();
        issues.addAll(validarProductosSinProveedor());
        issues.addAll(validarOrdenesSinProveedor());
        return issues;
    }

    public void compactar() {
        try (Connection conn = DBHelper.getConnection()) {
            conn.createStatement().execute("VACUUM;");
        } catch (SQLException e) {
            Logger.error(DatabaseIntegrityService.class, "Error compactando la base de datos", e);
        }
    }

    private List<String> validarProductosSinProveedor() {
        String sql = """
            SELECT codigo
            FROM productos
            WHERE proveedor_codigo IS NOT NULL
              AND proveedor_codigo NOT IN (SELECT codigo FROM proveedores)
        """;

        return ejecutarConsulta(sql, "Producto sin proveedor vinculado: %s");
    }

    private List<String> validarOrdenesSinProveedor() {
        String sql = """
            SELECT codigo
            FROM ordenes
            WHERE proveedor_codigo IS NOT NULL
              AND proveedor_codigo NOT IN (SELECT codigo FROM proveedores)
        """;

        return ejecutarConsulta(sql, "Orden sin proveedor vinculado: %s");
    }

    private List<String> ejecutarConsulta(String sql, String plantillaMensaje) {
        List<String> issues = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                issues.add(plantillaMensaje.formatted(rs.getString("codigo")));
            }
        } catch (SQLException e) {
            Logger.error(DatabaseIntegrityService.class, "Error validando integridad", e);
        }
        return issues;
    }
}
