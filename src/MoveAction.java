public class MoveAction extends MowerAction {
    private int magnitude;
    private Direction newDirection;

    public MoveAction(int magnitude, Direction newDirection) {
        this.magnitude = magnitude;
        this.newDirection = newDirection;
    }

    public int getMagnitude() {
        return magnitude;
    }

    public Direction getNewDirection() {
        return newDirection;
    }

    @Override
    public String getName() {
        return "move," + magnitude + "," + newDirection.name();
    }

    @Override
    public boolean validate() {
        return getMagnitude() >= 0 && getMagnitude() <= 2;
    }
}
