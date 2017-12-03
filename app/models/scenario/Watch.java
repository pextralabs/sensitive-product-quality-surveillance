package models.scenario;

public class Watch {
    private Person watcher;
    private Batch target;

    public Watch(Person watcher, Batch target) {
        this.watcher = watcher;
        this.target = target;
    }

    public Person getWatcher() {
        return watcher;
    }

    public void setWatcher(Person watcher) {
        this.watcher = watcher;
    }

    public Batch getTarget() {
        return target;
    }

    public void setTarget(Batch target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "Watcher: " + watcher + " Target: " +target;
    }
}
