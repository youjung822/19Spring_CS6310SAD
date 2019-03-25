public class CrashFeedback extends MowerFeedback {

    public CrashFeedback(MowerAction action) {
        this.action = action;
    }

    @Override
    protected String getName() {
        return "crash";
    }
}
