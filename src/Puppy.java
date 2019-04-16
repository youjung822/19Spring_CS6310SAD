import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class Puppy {
    public int stayPercentage;
    private int id;
    private Location location;

    public Puppy(int id, int stayPercentage, Location location, SimulationMonitor monitor) {
        this.id = id;
        this.stayPercentage = stayPercentage;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String pollForAction() {
        int r = ThreadLocalRandom.current().nextInt(0, 100);
        if (r < stayPercentage) {
            return "stay";
        } else {
            return "move";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Puppy puppy = (Puppy) o;
        return id == puppy.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
