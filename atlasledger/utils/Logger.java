package atlasledger.utils;

import java.time.LocalDateTime;

public final class Logger {

    private Logger() {
    }

    public static void info(Class<?> source, String message) {
        System.out.printf("[%s] [INFO] [%s] %s%n", LocalDateTime.now(), source.getSimpleName(), message);
    }

    public static void warn(Class<?> source, String message) {
        System.out.printf("[%s] [WARN] [%s] %s%n", LocalDateTime.now(), source.getSimpleName(), message);
    }

    public static void error(Class<?> source, String message, Throwable throwable) {
        System.err.printf("[%s] [ERROR] [%s] %s%n", LocalDateTime.now(), source.getSimpleName(), message);
        if (throwable != null) {
            throwable.printStackTrace(System.err);
        }
    }
}
