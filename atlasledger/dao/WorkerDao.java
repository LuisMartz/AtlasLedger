package atlasledger.dao;

import atlasledger.model.Worker;
import atlasledger.utils.DBHelper;
import atlasledger.utils.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public final class WorkerDao {

    private WorkerDao() {
    }

    public static Optional<Worker> findByUsername(String username) {
        String sql = """
            SELECT id, username, password_hash, nombre, rol, last_login
            FROM workers
            WHERE username = ?
        """;
        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            Logger.error(WorkerDao.class, "Error buscando trabajador", e);
        }
        return Optional.empty();
    }

    public static void save(Worker worker) {
        String sql = """
            INSERT INTO workers (username, password_hash, nombre, rol, last_login)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(username) DO UPDATE SET
                password_hash = excluded.password_hash,
                nombre = excluded.nombre,
                rol = excluded.rol,
                last_login = excluded.last_login
        """;
        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, worker.getUsername().toLowerCase());
            ps.setString(2, worker.getPasswordHash());
            ps.setString(3, worker.getNombre());
            ps.setString(4, worker.getRol());
            ps.setString(5, worker.getLastLogin() != null ? worker.getLastLogin().toString() : null);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.error(WorkerDao.class, "Error guardando trabajador", e);
        }
    }

    public static void updateLastLogin(int workerId) {
        String sql = "UPDATE workers SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, workerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.error(WorkerDao.class, "Error actualizando ultimo acceso", e);
        }
    }

    public static boolean hasAnyWorker() {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS total FROM workers");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
        } catch (SQLException e) {
            Logger.error(WorkerDao.class, "Error verificando trabajadores", e);
        }
        return false;
    }

    private static Worker map(ResultSet rs) throws SQLException {
        Worker worker = new Worker();
        worker.setId(rs.getInt("id"));
        worker.setUsername(rs.getString("username"));
        worker.setPasswordHash(rs.getString("password_hash"));
        worker.setNombre(rs.getString("nombre"));
        worker.setRol(rs.getString("rol"));
        String lastLogin = rs.getString("last_login");
        if (lastLogin != null) {
            worker.setLastLogin(LocalDateTime.parse(lastLogin.replace(" ", "T")));
        }
        return worker;
    }
}
