package atlasledger.model;

import java.time.LocalDateTime;

public class AppLog {

    private int id;
    private String level;
    private String source;
    private String message;
    private LocalDateTime createdAt;

    public AppLog() {
    }

    public AppLog(int id, String level, String source, String message, LocalDateTime createdAt) {
        this.id = id;
        this.level = level;
        this.source = source;
        this.message = message;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
