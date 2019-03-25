public class TurnOffAction extends MowerAction {
    @Override
    public String getName() {
        return "turn_off";
    }

    @Override
    public boolean validate() {
        return true;
    }
}
