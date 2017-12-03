package models.scenario;

import br.ufes.inf.lprm.context.model.Entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Container extends Entity {
    private Location location;
    private Set<Batch> batches;
    private Temperature temperature;

    public Container(String id, Batch... batches) {
        super(id);
        this.batches = new HashSet<>(Arrays.asList(batches));
        this.batches.forEach(batch -> batch.setContainer(this));
    }

    public void addBatches(Batch... batches) {
        this.batches.addAll(Arrays.asList(batches));
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Set<Batch> getBatches() {
        return batches;
    }

    public void setBatches(Set<Batch> batches) {
        this.batches = batches;
    }

    @Override
    public String toString() {
        return "Container '" + id + "'";
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public void setTemperature(Temperature temperature) {
        this.temperature = temperature;
    }
}
