public class ScanAction extends MowerAction {
    @Override
    public String getName() {
        return "scan";
    }

    @Override
    public boolean validate() {
        return true;
    }
}
