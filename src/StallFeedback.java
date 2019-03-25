public class StallFeedback extends MowerFeedback {
    private int distance;

    public StallFeedback(int distance, MowerAction action) {
        this.action = action;
        this.distance = distance;
    }

    public int getDistance() {
        return distance;
    }

    @Override
    protected String getName() {
        return "stall," + distance;
    }
}
