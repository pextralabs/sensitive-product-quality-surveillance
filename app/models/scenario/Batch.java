package models.scenario;

import br.ufes.inf.lprm.context.model.Entity;

public class Batch extends Entity {
    private ProductType productType;
    private Container container;
    private TimeToThreshold ttt;

    public Batch(String id, ProductType productType) {
        super(id);
        this.productType = productType;
    }
    public Batch(String id, ProductType productType, Container container) {
        super(id);
        this.productType = productType;
        this.container = container;
        container.addBatches(this);
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public TimeToThreshold getTtt() {
        return ttt;
    }

    public void setTtt(TimeToThreshold ttt) {
        this.ttt = ttt;
    }

    @Override
    public String toString() {
        return "Batch of " + productType.getName() + " '" + id + "'";
    }
}
