package atlasledger.simulation;

import java.time.LocalDateTime;

public class TransportEvent {

    private final String shipmentId;
    private final String origin;
    private final String destination;
    private final String status;
    private final LocalDateTime eta;

    public TransportEvent(String shipmentId, String origin, String destination, String status, LocalDateTime eta) {
        this.shipmentId = shipmentId;
        this.origin = origin;
        this.destination = destination;
        this.status = status;
        this.eta = eta;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getEta() {
        return eta;
    }
}
