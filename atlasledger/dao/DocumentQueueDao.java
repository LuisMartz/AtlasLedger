package atlasledger.dao;

import atlasledger.model.DocumentTask;
import atlasledger.utils.DBHelper;
import atlasledger.utils.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class DocumentQueueDao {

    private DocumentQueueDao() {
    }

    public static void enqueue(DocumentTask task) {
        String sql = """
            INSERT INTO document_queue (file_name, local_path, status, uploaded_at, metadata)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, task.getFileName());
            ps.setString(2, task.getLocalPath());
            ps.setString(3, task.getStatus());
            ps.setString(4, task.getUploadedAt() != null ? task.getUploadedAt().toString() : null);
            ps.setString(5, task.getMetadata());
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.error(DocumentQueueDao.class, "Error insertando documento en cola", e);
        }
    }

    public static List<DocumentTask> pending() {
        String sql = """
            SELECT id, file_name, local_path, status, uploaded_at, metadata
            FROM document_queue
            WHERE status = 'PENDING'
            ORDER BY id ASC
        """;
        List<DocumentTask> tasks = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tasks.add(map(rs));
            }
        } catch (SQLException e) {
            Logger.error(DocumentQueueDao.class, "Error consultando documentos pendientes", e);
        }
        return tasks;
    }

    public static void updateStatus(int id, String status) {
        String sql = """
            UPDATE document_queue
            SET status = ?, uploaded_at = CASE WHEN ? = 'DONE' THEN CURRENT_TIMESTAMP ELSE uploaded_at END
            WHERE id = ?
        """;
        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, status);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.error(DocumentQueueDao.class, "Error actualizando estado de documento", e);
        }
    }

    private static DocumentTask map(ResultSet rs) throws SQLException {
        DocumentTask task = new DocumentTask();
        task.setId(rs.getInt("id"));
        task.setFileName(rs.getString("file_name"));
        task.setLocalPath(rs.getString("local_path"));
        task.setStatus(rs.getString("status"));
        String uploadedAt = rs.getString("uploaded_at");
        if (uploadedAt != null) {
            task.setUploadedAt(LocalDateTime.parse(uploadedAt.replace(" ", "T")));
        }
        task.setMetadata(rs.getString("metadata"));
        return task;
    }
}
