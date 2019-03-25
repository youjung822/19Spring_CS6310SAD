public abstract class MowerAction {
    protected abstract String getName();

    public abstract boolean validate();

    @Override
    public String toString() {
        return getName();
    }
}
