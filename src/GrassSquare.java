public class GrassSquare extends Square {

    private boolean empty;

    public GrassSquare() {
        empty = false;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void cut() {
        empty = true;
    }

    @Override
    protected String getName() {
        if (empty)
            return "empty";
        else
            return "grass";
    }
}
