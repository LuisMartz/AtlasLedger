package atlasledger.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public final class Logger {

    private static final String INSERT_LOG_SQL = """
        INSERT INTO app_logs (level, source, message, created_at)
        VALUES (?, ?, ?, CURRENT_TIMESTAMP)
    """;
    private static volatile boolean persistenceEnabled = true;

    private Logger() {
    }

    public static void info(Class<?> source, String message) {
        log("INFO", source, message, null);
    }

    public static void warn(Class<?> source, String message) {
        log("WARN", source, message, null);
    }

    public static void error(Class<?> source, String message, Throwable throwable) {
        log("ERROR", source, message, throwable);
    }

    public static void error(Class<?> source, String message) {
        log("ERROR", source, message, null);
    }

    private static void log(String level, Class<?> source, String message, Throwable throwable) {
        LocalDateTime now = LocalDateTime.now();
        if ("ERROR".equals(level)) {
            System.err.printf("[%s] [%s] [%s] %s%n", now, level, source.getSimpleName(), message);
            if (throwable != null) {
                throwable.printStackTrace(System.err);
            }
        } else {
            System.out.printf("[%s] [%s] [%s] %s%n", now, level, source.getSimpleName(), message);
        }
        persist(level, source, message);
    }

    private static void persist(String level, Class<?> source, String message) {
        if (!persistenceEnabled) {
            return;
        }
        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(INSERT_LOG_SQL)) {
            ps.setString(1, level);
            ps.setString(2, source.getSimpleName());
            ps.setString(3, message);
            ps.executeUpdate();
        } catch (SQLException ex) {
            persistenceEnabled = false;
        }
    }
}
