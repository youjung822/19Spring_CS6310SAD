import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class Puppy {
    public int stayPercentage;
    private int id;
    private Location location;
    private SimulationMonitor monitor;

    public Puppy(int id, int stayPercentage, Location location, SimulationMonitor monitor) {
        this.id = id;
        this.stayPercentage = stayPercentage;
        this.location = location;
        this.monitor = monitor;
    }

    public int getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public String pollForAction() {
        int r = ThreadLocalRandom.current().nextInt(0, 100);
        if (r < stayPercentage) {
            return "stay";
        } else {
            List<Location> candidates = monitor.getLocationsForPuppy(getLocation());
            int cSize = candidates.size();
            int cChosen = ThreadLocalRandom.current().nextInt(0, cSize);
            location = candidates.get(cChosen);
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
