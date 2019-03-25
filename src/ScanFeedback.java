public class ScanFeedback extends MowerFeedback {
    private String[] squares;

    public ScanFeedback(String[] squares, MowerAction action) {
        this.squares = squares;
        this.action = action;
    }


    public String[] getSquares() {
        return squares;
    }

    @Override
    protected String getName() {
        return String.join(",", squares);
    }
}
