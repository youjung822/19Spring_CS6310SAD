public abstract class Square {
    abstract protected String getName();

    @Override
    public String toString() {
        return getName();
    }
}
