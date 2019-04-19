import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SimulationMonitor {

    private boolean isStopped = false;
    private int turnCount = 0;

    private Lawn lawn;
    private List<Mower> mowers;
    private List<Puppy> puppies;
    private int collisionDelay;
    private int maxTurnCount;

    private Object nextObject;

    public int getTurnCount() {
        return turnCount;
    }

    public void setStopped() {
        boolean allCrashedOrOff = true;
        for (Mower mower : mowers) {
            if (!(mower.getStatus().equals(MowerStatus.Crashed) || mower.getStatus().equals(MowerStatus.TurnedOff))) {
                allCrashedOrOff = false;
                break;
            }
        }
        isStopped = (turnCount >= maxTurnCount) || allCrashedOrOff;
    }

    public boolean isStopped() {
        return isStopped;
    }

    private void validate(String fieldName, int actual, int min, int max) throws Exception {
        if (actual < min || actual > max) {
            throw new Exception(fieldName + "(" + actual + ") needs to be between " + min + " and " + max + " (inclusive).");
        }
    }

    public void setupUsingFile(String filePath) throws Exception {
        turnCount = 0;
        isStopped = false;

        final String DELIMITER = ",";

        Scanner takeCommand = new Scanner(new File(filePath));
        String[] tokens;
        int i, j, k;

        // read in the lawn information
        tokens = takeCommand.nextLine().split(DELIMITER);
        int lawnWidth = Integer.parseInt(tokens[0]);
        validate("lawnWidth", lawnWidth, 1, 15);
        tokens = takeCommand.nextLine().split(DELIMITER);
        int lawnHeight = Integer.parseInt(tokens[0]);
        validate("lawnHeight", lawnHeight, 1, 10);

        // read in the lawnmower starting information
        tokens = takeCommand.nextLine().split(DELIMITER);
        int numMowers = Integer.parseInt(tokens[0]);
        validate("numMowers", numMowers, 0, 10);

        mowers = new ArrayList<>(numMowers);

        tokens = takeCommand.nextLine().split(DELIMITER);
        collisionDelay = Integer.parseInt(tokens[0]);
        validate("collisionDelay", collisionDelay, 0, 4);

        MowerSharedState sharedState = new MowerSharedState();

        for (k = 1; k <= numMowers; k++) {
            tokens = takeCommand.nextLine().split(DELIMITER);
            Location l = new Location(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
            validate("mower.x", l.getX(), 0, lawnWidth - 1);
            validate("mower.y", l.getY(), 0, lawnHeight - 1);
            Direction d = Direction.valueOf(tokens[2]);
            mowers.add(new Mower(k, l, d, sharedState));
        }

        // read in the crater information
        tokens = takeCommand.nextLine().split(DELIMITER);
        int numCraters = Integer.parseInt(tokens[0]);
        Set<Location> craters = new HashSet<>(numCraters);
        for (k = 0; k < numCraters; k++) {
            tokens = takeCommand.nextLine().split(DELIMITER);
            Location l = new Location(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));

            // place a crater at the given location
            craters.add(l);
            validate("crater.x", l.getX(), 0, lawnWidth - 1);
            validate("crater.y", l.getY(), 0, lawnHeight - 1);
        }

        // read in the puppy information
        tokens = takeCommand.nextLine().split(DELIMITER);
        int numPuppies = Integer.parseInt(tokens[0]);
        validate("numPuppies", numPuppies, 0, 6);
        puppies = new ArrayList<>(numPuppies);

        tokens = takeCommand.nextLine().split(DELIMITER);
        int stayPercentage = Integer.parseInt(tokens[0]);
        validate("stayPercentage", stayPercentage, 0, 100);

        sharedState.setPuppyStayPercentage(stayPercentage);

        for (k = 1; k <= numPuppies; k++) {
            tokens = takeCommand.nextLine().split(DELIMITER);
            Location l = new Location(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
            validate("puppy.x", l.getX(), 0, lawnWidth - 1);
            validate("puppy.y", l.getY(), 0, lawnHeight - 1);
            puppies.add(new Puppy(k, stayPercentage, l, this));
        }

        tokens = takeCommand.nextLine().split(DELIMITER);
        maxTurnCount = Integer.parseInt(tokens[0]);
        validate("maxTurnCount", maxTurnCount, 0, 300);

        // generate the lawn information
        Map<Location, Square> squares = new HashMap<>(lawnHeight * lawnWidth);
        for (i = 0; i < lawnWidth; i++) {
            for (j = 0; j < lawnHeight; j++) {

                Location l = new Location(i, j);
                if (craters.contains(l)) {
                    squares.put(l, new CraterSquare());
                } else {
                    squares.put(l, new GrassSquare());
                }
            }
        }

        for (Mower mower : mowers) {
            ((GrassSquare) squares.get(mower.getLocation())).cut();
        }

        lawn = new Lawn(lawnWidth, lawnHeight, squares);

        takeCommand.close();

        nextObject = mowers.get(0);
    }

    public int getGrassRemaining() {
        int count = 0;
        for (Square sq : lawn.getSquares().values()) {
            if (sq instanceof GrassSquare && !((GrassSquare) sq).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public int getGrassCut() {
        int count = 0;
        for (Square sq : lawn.getSquares().values()) {
            if (sq instanceof GrassSquare && ((GrassSquare) sq).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public int getLawnWidth() {
        return lawn.getWidth();
    }

    public int getLawnHeight() {
        return lawn.getHeight();
    }

    public Square getSquare(Location l) {
        return lawn.getSquare(l);
    }

    public List<Mower> getMowers() {
        return mowers;
    }

    public List<Puppy> getPuppies() {
        return puppies;
    }

    public String report() {
        if (!isStopped) {
            isStopped = true;
        }
        int totalSquares = 0;
        int totalGrass = 0;
        int totalClean = 0;
        for (int x = 0; x < lawn.getWidth(); x++) {
            for (int y = 0; y < lawn.getHeight(); y++) {
                Location l = new Location(x, y);
                Square s = lawn.getSquare(l);
                if (s instanceof GrassSquare) {
                    totalGrass++;
                    GrassSquare g = (GrassSquare) s;
                    if (g.isEmpty()) {
                        totalClean++;
                    }
                }
                totalSquares++;
            }
        }
        return totalSquares + "," + totalGrass + "," + totalClean + "," + turnCount;
    }

    public void fastForward() {
        while (!isStopped) {
            next();
        }
    }

    public void next() {
        if (!isStopped) {
            if (nextObject instanceof Mower) {
                Mower m = (Mower) nextObject;
                if (m.getStatus().equals(MowerStatus.TurnedOn)) {
                    Main.writeln("mower," + m.getId());
                    MowerAction action = m.pollForAction();
                    MowerFeedback feedback = null;
                    Main.writeln(action.toString());
                    if (validateMowerAction(action)) {
                        feedback = createMowerFeedback(m, action);
                    } else {
                        feedback = new CrashFeedback(action);
                    }
                    Main.writeln(feedback.toString());
                    m.processFeedback(feedback);
                }

                if (m.getStatus().equals(MowerStatus.Stalled)) {
                    int rts = m.getRemainingTurnsStalled();
                    if (rts == 1) {
                        m.setRemainingTurnsStalled(0);
                        m.setStatus(MowerStatus.TurnedOn);
                    } else if (rts > 1) {
                        m.setRemainingTurnsStalled(m.getRemainingTurnsStalled() - 1);
                    }
                }

                int nextMowerId = m.getId() + 1;
                if (nextMowerId <= mowers.size()) {
                    nextObject = mowers.get(nextMowerId - 1);
                } else {
                    if (puppies.isEmpty()) {
                        nextObject = mowers.get(0);
                    } else {
                        nextObject = puppies.get(0);
                    }
                }
            } else {
                Puppy p = (Puppy) nextObject;
                Main.writeln("puppy," + p.getId());
                Location oldLocation = p.getLocation();
                String action = p.pollForAction();
                if (action.equals("stay")) {
                    Main.writeln(action);
                } else {
                    List<Location> candidates = getLocationsForPuppy(oldLocation);
                    int cSize = candidates.size();
                    Location newLocation = null;
                    if (cSize > 0) {
                        int cChosen = ThreadLocalRandom.current().nextInt(0, cSize);
                        newLocation = candidates.get(cChosen);
                        p.setLocation(newLocation);
                        Main.writeln(action + "," + p.getLocation());
                    } else {
                        newLocation = oldLocation;
                        Main.writeln("stay"); // THANK YOU DR. MOSS
                    }
                    updateMowersAfterPuppyMoves(oldLocation, newLocation);

                }
                int nextPuppyId = p.getId() + 1;
                if (nextPuppyId <= puppies.size()) {
                    nextObject = puppies.get(nextPuppyId - 1);
                } else {
                    turnCount++;
                    nextObject = mowers.get(0);
                }
                Main.writeln("ok");
            }
        }

        setStopped();
    }

    private void updateMowersAfterPuppyMoves(Location oldLocation, Location newLocation) {
        if (!oldLocation.equals(newLocation)) {
            for (Mower m : mowers) {
                if (m.getLocation().equals(oldLocation)) {
                    m.setStatus(MowerStatus.TurnedOn);
                }
                if (m.getLocation().equals(newLocation)) {
                    m.setStatus(MowerStatus.Stalled);
                    m.setRemainingTurnsStalled(0);
                }
            }
        }
    }

    public Object getNextObject() {
        return nextObject;
    }

    private List<Location> getLocationsForPuppy(Location l) {
        Location[] ns = l.getNeighborLocations();
        List<Location> candidates = new ArrayList<>();
        for (Location n : ns) {
            if (lawn.getSquares().containsKey(n) &&
                    lawn.getSquare(n) instanceof GrassSquare &&
                    !isPuppyAtLocation(n)) {
                candidates.add(n);
            }
        }
        return candidates;
    }

    private boolean isPuppyAtLocation(Location l) {
        for (Puppy p : puppies) {
            if (p.getLocation().equals(l)) {
                return true;
            }
        }
        return false;
    }

    private boolean validateMowerAction(MowerAction mowerAction) {
        return mowerAction.validate();
    }

    private MowerFeedback createMowerFeedback(Mower mower, MowerAction mowerAction) {
        if (mowerAction instanceof ScanAction) {
            String[] names = lawn.scan(mower.getLocation());
            Location[] neighbors = mower.getLocation().getNeighborLocations();
            for (int i = 0; i < 8; i++) {
                Location n = neighbors[i];
                for (Puppy p : puppies) {
                    if (p.getLocation().equals(n)) {
                        names[i] = "puppy_" + names[i];
                    }
                }

                for (Mower m : mowers) {
                    if (m.getLocation().equals(n)) {
                        if (names[i].startsWith("puppy")) {
                            names[i] = "puppy_mower";
                        } else {
                            names[i] = "mower";
                        }
                    }
                }
            }
            return new ScanFeedback(names, mowerAction);
        } else if (mowerAction instanceof MoveAction) {
            Map<Integer, Location> targets = getTargetMowerLocations(mower, (MoveAction) mowerAction);
            if (targets.size() == 1) {
                return new OKFeedback(mowerAction);
            }
            if (targets.size() >= 2) {
                Location target = targets.get(new Integer(1));
                if (!lawn.isGrass(target)) {
                    return new CrashFeedback(mowerAction);
                } else {
                    lawn.cut(target);
                }

                if (findPuppy(target) != null) {
                    return new StallFeedback(1, mowerAction);
                }
                if (findOtherMower(target, mower) != null) {
                    mower.setRemainingTurnsStalled(collisionDelay);
                    return new StallFeedback(0, mowerAction);
                }
            }

            if (targets.size() == 3) {
                Location target = targets.get(new Integer(2));
                if (!lawn.isGrass(target)) {
                    return new CrashFeedback(mowerAction);
                } else {
                    lawn.cut(target);
                }

                if (findPuppy(target) != null) {
                    return new StallFeedback(2, mowerAction);
                }
                if (findOtherMower(target, mower) != null) {
                    mower.setRemainingTurnsStalled(collisionDelay);
                    return new StallFeedback(1, mowerAction);
                }
            }
            return new OKFeedback(mowerAction);

        } else if (mowerAction instanceof TurnOffAction) {
            return new OKFeedback(mowerAction);
        } else {
            return new CrashFeedback(mowerAction);
        }
    }

    private Map<Integer, Location> getTargetMowerLocations(Mower mower, MoveAction mowerAction) {
        Location l = mower.getLocation();
        Direction d = mower.getDirection();
        Map<Integer, Location> result = new HashMap<>(mowerAction.getMagnitude());
        if (mowerAction.getMagnitude() >= 0) {
            result.put(new Integer(0), l);
        }
        if (mowerAction.getMagnitude() >= 1) {
            result.put(new Integer(1), l.getMovedLocation(1, d));
        }
        if (mowerAction.getMagnitude() == 2) {
            result.put(new Integer(2), l.getMovedLocation(2, d));
        }

        return result;
    }

    private Mower findOtherMower(Location location, Mower mower) {
        for (Mower om : mowers) {
            if (om.getLocation().equals(location) && om.getId() != mower.getId()) {
                return om;
            }
        }
        return null;
    }

    private Puppy findPuppy(Location location) {
        for (Puppy op : puppies) {
            if (op.getLocation().equals(location)) {
                return op;
            }
        }
        return null;
    }

    private boolean isMower(Location l) {
        for (Mower m : mowers) {
            if (m.getLocation().equals(l)) {
                return true;
            }
        }
        return false;
    }

    public void renderMowerState() {
        if (mowers != null && !mowers.isEmpty()) {
            MowerSharedState state = mowers.get(0).getSharedState();
            int i, j;
            int charWidth = 2 * lawn.getWidth() + 2 + 2;

            // display the rows of the lawn from top to bottom
            System.out.println("^");
            System.out.println("*");
            System.out.println("*");
            System.out.println("*");
            for (j = lawn.getHeight() + 2 - 1; j >= 0; j--) {
                renderHorizontalBar(charWidth);

                // display the Y-direction identifier
                System.out.print(j);

                // display the contents of each square on this row
                for (i = 0; i < lawn.getWidth() + 2; i++) {
                    System.out.print("|");
                    if (state.getPuppyLocations().contains(new Location(i, j))) {
                        System.out.print("p");
                    } else if (isMower(new Location(i, j))) {
                        System.out.print("m");
                    } else {

                        Square s = state.getSquares().get(new Location(i, j));
                        if (s == null) {
                            System.out.print("?");
                        } else if (s instanceof GrassSquare && ((GrassSquare) s).isEmpty()) {
                            System.out.print(" ");
                        } else if (s instanceof GrassSquare) {
                            System.out.print("G");
                        } else if (s instanceof CraterSquare) {
                            System.out.print("C");
                        } else {
                            System.out.print("F");
                        }
                    }
                }
                System.out.println("|");
            }
            renderHorizontalBar(charWidth);

            // display the column X-direction identifiers
            System.out.print(" ");
            for (i = 0; i < lawn.getWidth() + 2; i++) {
                System.out.print(" " + i);
            }
            System.out.println(" * * * >");
        }
    }


    private void renderHorizontalBar(int size) {
        System.out.print(" ");
        for (int k = 0; k < size; k++) {
            System.out.print("-");
        }
        System.out.println();
    }

}
