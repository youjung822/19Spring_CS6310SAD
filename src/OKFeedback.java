public class OKFeedback extends MowerFeedback {

    public OKFeedback(MowerAction action) {
        this.action = action;
    }

    @Override
    protected String getName() {
        return "ok";
    }
}
