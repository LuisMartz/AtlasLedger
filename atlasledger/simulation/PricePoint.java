package atlasledger.simulation;

import java.time.LocalDateTime;

public class PricePoint {
    private final String productCode;
    private final LocalDateTime time;
    private final double price;

    public PricePoint(String productCode, LocalDateTime time, double price) {
        this.productCode = productCode;
        this.time = time;
        this.price = price;
    }

    public String getProductCode() {
        return productCode;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public double getPrice() {
        return price;
    }
}
