import java.util.Objects;

public class Location {
    private int x;
    private int y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Location[] getNeighborLocations() {
        Location[] neighborLocations = {
                new Location(x, y + 1),
                new Location(x + 1, y + 1),
                new Location(x + 1, y),
                new Location(x + 1, y - 1),
                new Location(x, y - 1),
                new Location(x - 1, y - 1),
                new Location(x - 1, y),
                new Location(x - 1, y + 1)
        };
        return neighborLocations;
    }

    public Location getMovedLocation(int moveMagnitude, Direction direction) {
        Location finalLocation = this;
        if (direction.equals(Direction.north)) {
            finalLocation = new Location(x, y + moveMagnitude);
        } else if (direction.equals(Direction.northeast)) {
            finalLocation = new Location(x + moveMagnitude, y + moveMagnitude);
        } else if (direction.equals(Direction.east)) {
            finalLocation = new Location(x + moveMagnitude, y);
        } else if (direction.equals(Direction.southeast)) {
            finalLocation = new Location(x + moveMagnitude, y - moveMagnitude);
        } else if (direction.equals(Direction.south)) {
            finalLocation = new Location(x, y - moveMagnitude);
        } else if (direction.equals(Direction.southwest)) {
            finalLocation = new Location(x - moveMagnitude, y - moveMagnitude);
        } else if (direction.equals(Direction.west)) {
            finalLocation = new Location(x - moveMagnitude, y);
        } else if (direction.equals(Direction.northwest)) {
            finalLocation = new Location(x - moveMagnitude, y + moveMagnitude);
        }
        return finalLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location that = (Location) o;
        return getX() == that.getX() &&
                getY() == that.getY();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY());
    }

    @Override
    public String toString() {
        return "" + x + ',' + y;
    }
}
