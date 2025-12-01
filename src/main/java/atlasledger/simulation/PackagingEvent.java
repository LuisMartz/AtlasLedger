package atlasledger.simulation;

import java.time.LocalDateTime;

public class PackagingEvent {

    private final String productCode;
    private final String batchId;
    private final String status;
    private final LocalDateTime timestamp;

    public PackagingEvent(String productCode, String batchId, String status, LocalDateTime timestamp) {
        this.productCode = productCode;
        this.batchId = batchId;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getBatchId() {
        return batchId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
