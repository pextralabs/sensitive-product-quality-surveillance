package models.scenario;

public class ProductType {
    private  String name;
    private  double maxThreshold;
    private  double minThreshold;

    public ProductType(String name, double maxThreshold, double minThreshold) {
        this.name = name;
        this.maxThreshold = maxThreshold;
        this.minThreshold = minThreshold;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMaxThreshold() {
        return maxThreshold;
    }

    public void setMaxThreshold(double maxThreshold) {
        this.maxThreshold = maxThreshold;
    }

    public double getMinThreshold() {
        return minThreshold;
    }

    public void setMinThreshold(double minThreshold) {
        this.minThreshold = minThreshold;
    }

    @Override
    public String toString() {
        return "ProductType: " + name;
    }
}
