package atlasledger.simulation;

public class TransportMode {

    private String name;
    private double costPerKg;
    private double speedHours;
    private double riskFactor;

    public TransportMode() {
    }

    public TransportMode(String name, double costPerKg, double speedHours, double riskFactor) {
        this.name = name;
        this.costPerKg = costPerKg;
        this.speedHours = speedHours;
        this.riskFactor = riskFactor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCostPerKg() {
        return costPerKg;
    }

    public void setCostPerKg(double costPerKg) {
        this.costPerKg = costPerKg;
    }

    public double getSpeedHours() {
        return speedHours;
    }

    public void setSpeedHours(double speedHours) {
        this.speedHours = speedHours;
    }

    public double getRiskFactor() {
        return riskFactor;
    }

    public void setRiskFactor(double riskFactor) {
        this.riskFactor = riskFactor;
    }
}
