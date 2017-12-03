package models.scenario;

import br.ufes.inf.lprm.context.model.Entity;

import java.util.HashSet;
import java.util.Set;

public class Person extends Entity {
    private Location location;
    private Speed speed;
    private Set<Watch> watchers;

    public Person(String id) {
        super(id);
        this.watchers = new HashSet<>();
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Speed getSpeed() {
        return speed;
    }

    public void setSpeed(Speed speed) {
        this.speed = speed;
    }

    public Set<Watch> getWatchers() {
        return watchers;
    }

    public void setWatchers(Set<Watch> watchers) {
        this.watchers = watchers;
    }

    @Override
    public String toString() {
        return "Person: " + id;
    }

    static public LatLng walk(Person person, double x, double y) {
        double earthRadius = (6.37814) * Math.pow(10, 6);
        LatLng latLng = person.getLocation().getValue().getValue();
        double nextLatitude = latLng.getLatitude() + (y / earthRadius) * (180 / Math.PI);
        double nextLongitude = latLng.getLongitude() + (x / earthRadius) * (180 / Math.PI) / Math.cos(latLng.getLatitude() * Math.PI / 180);
        return new LatLng(nextLatitude, nextLongitude);
    }
}
