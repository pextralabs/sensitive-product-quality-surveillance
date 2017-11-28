package models;

import javax.persistence.ManyToOne;

@javax.persistence.Entity
public class Sensor extends Persistent {

    @ManyToOne(optional=false)
    private Entity bearer;
    private String label;
    private Double value;

    public Sensor(Entity bearer,  String name, Double defaultValue) {
        this.bearer = bearer;
        this.label = name;
        this.value = defaultValue;
    }

    public Entity getBearer() {
        return bearer;
    }

    public String getLabel() {
        return label;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
