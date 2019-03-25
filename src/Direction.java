import java.util.ArrayList;
import java.util.List;

public enum Direction {
    north(0), northeast(1), east(2), southeast(3), south(4), southwest(5), west(6), northwest(7);

    private int clockwiseIndex;

    Direction(int clockwiseIndex) {
        this.clockwiseIndex = clockwiseIndex;
    }

    public static Direction getById(int id) {
        for (Direction d : values()) {
            if (d.getClockwiseIndex() == id)
                return d;
        }
        throw new RuntimeException("Direction not found for id: " + id);
    }

    public int getClockwiseIndex() {
        return clockwiseIndex;
    }

    public List<Direction> clockwiseNextDirections() {
        List<Direction> directions = new ArrayList<>();
        for (int i = getClockwiseIndex(); i < 8; i++) {
            directions.add(getById(i));
        }
        for (int i = 0; i < getClockwiseIndex(); i++) {
            directions.add(getById(i));
        }
        return directions;
    }

    public String getShortName() {
        if (this.equals(north)) return "N";
        else if (this.equals(northeast)) return "NE";
        else if (this.equals(east)) return "E";
        else if (this.equals(southeast)) return "SE";
        else if (this.equals(south)) return "S";
        else if (this.equals(southwest)) return "SW";
        else if (this.equals(west)) return "W";
        else if (this.equals(northwest)) return "NW";
        else return "?";
    }

    public Direction getOppositeDirection() {
        return clockwiseNextDirections().get(4);
    }
}
