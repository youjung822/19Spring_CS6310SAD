public abstract class MowerFeedback {
    protected MowerAction action;

    protected abstract String getName();

    public MowerAction getAction() {
        return action;
    }

    @Override
    public String toString() {
        return getName();
    }
}
