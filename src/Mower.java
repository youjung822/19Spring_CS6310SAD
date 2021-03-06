import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Mower {
    private int id;
    private Location location;
    private Direction direction;
    private MowerStatus status;
    private int remainingTurnsStalled;
    private MowerSharedState sharedState;

    private Queue<MoveAction> actionQueue = new LinkedList<>();

    private Direction lastRandomDirection = null;

    public Mower(int id, Location location, Direction direction, MowerSharedState sharedState) {
        this.id = id;
        this.location = location;
        this.direction = direction;
        this.status = MowerStatus.TurnedOn;
        this.remainingTurnsStalled = 0;
        this.sharedState = sharedState;
        sharedState.register(this);
    }

    public MowerSharedState getSharedState() {
        return sharedState;
    }

    public int getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public Direction getDirection() {
        return direction;
    }

    public MowerStatus getStatus() {
        return status;
    }

    public void setStatus(MowerStatus status) {
        this.status = status;
    }

    public int getRemainingTurnsStalled() {
        return remainingTurnsStalled;
    }

    public void setRemainingTurnsStalled(int remainingTurnsStalled) {
        this.remainingTurnsStalled = remainingTurnsStalled;
    }

    public MowerAction pollForAction() {
        if (getId() == 1 && sharedState.getPuppyStayPercentage() != 100) {
            sharedState.expirePuppyLocations();
        }

        if (sharedState.isWorkPending()) {
            if (!actionQueue.isEmpty()) {
                MoveAction a = actionQueue.remove();
                int m = a.getMagnitude();
                for (int i = 1; i <= m; i++) {
                    Location destination = location.getMovedLocation(i, direction);
                    if (!sharedState.isSafeLocation(destination)) {
                        actionQueue.clear();
                        return pollForAction();
                    }
                }
                return a;
            } else if (sharedState.isScanDone(location)) {
                //starting with current direction
                List<Direction> directionCandidates = direction.clockwiseNextDirections();

                // search for the max distance mower should go
                for (Direction directionCandidate : directionCandidates) {
                    int maxDistance = 1;
                    boolean hasUnCutGrass = false;
                    int lastGrassDistance = 0;
                    while (true) {
                        Location destinationLocation = location.getMovedLocation(maxDistance, directionCandidate);
                        if (sharedState.getSquares().containsKey(destinationLocation)
                                && sharedState.isSafeLocation(destinationLocation)) {
                            Square destinationSquare = sharedState.getSquares().get(destinationLocation);
                            if (destinationSquare instanceof GrassSquare) {
                                GrassSquare destinationGrass = (GrassSquare) destinationSquare;
                                if (sharedState.isScanDone(destinationLocation)) {
                                    if (!destinationGrass.isEmpty()) {
                                        lastGrassDistance = maxDistance;
                                        hasUnCutGrass = true;
                                    }
                                    maxDistance++;
                                } else {
                                    return createMoveAction(maxDistance, directionCandidate);
                                }
                            } else {
                                if (hasUnCutGrass)
                                    return createMoveAction(lastGrassDistance, directionCandidate);
                                else
                                    break;
                            }
                        } else {
                            if (hasUnCutGrass)
                                return createMoveAction(lastGrassDistance, directionCandidate);
                            else
                                break;
                        }
                    }
                }

                // if there is no place to go, pick a neighboring grass randomly
                List<Direction> grassNeighbors = new ArrayList<>();
                for (Direction directionCandidate : directionCandidates) {
                    Location neighbor = location.getMovedLocation(1, directionCandidate);
                    Square ns = sharedState.getSquares().get(neighbor);
                    if (ns instanceof GrassSquare && sharedState.isSafeLocation(neighbor)) {
                        grassNeighbors.add(directionCandidate);
                    }
                }

                //avoid going back
                if (grassNeighbors.size() > 1 && lastRandomDirection != null) {
                    grassNeighbors.remove(lastRandomDirection.getOppositeDirection());
                }
                int size = grassNeighbors.size();
                Direction randomDirection = grassNeighbors.get(ThreadLocalRandom.current().nextInt(0, size));
                lastRandomDirection = randomDirection;
                return createMoveAction(1, randomDirection);

            } else {
                return new ScanAction();
            }
        } else {
            return new TurnOffAction();
        }

    }

    private MowerAction createMoveAction(int maxDistance, Direction directionCandidate) {
        int remainingDistance = maxDistance;
        while (remainingDistance > 0) {
            if (remainingDistance >= 2) {
                actionQueue.add(new MoveAction(2, directionCandidate));
                remainingDistance = remainingDistance - 2;
            } else {
                actionQueue.add(new MoveAction(1, directionCandidate));
                remainingDistance = remainingDistance - 1;
            }
        }

        if (directionCandidate.equals(direction)) {
            return actionQueue.remove();
        } else {
            return new MoveAction(0, directionCandidate);
        }
    }

    public void processFeedback(MowerFeedback feedback) {
        if (feedback instanceof ScanFeedback) {
            String[] names = ((ScanFeedback) feedback).getSquares();
            for (int i = 0; i < 8; i++) {
                Direction direction = Direction.getById(i);
                Location l = location.getMovedLocation(1, direction);
                String name = names[i];
                if (name.equals("grass")) {
                    sharedState.setSquare(l, new GrassSquare());
                } else if (name.equals("puppy_grass")) {
                    sharedState.setSquare(l, new GrassSquare());
                    sharedState.addPuppyLocation(l);
                } else if (name.equals("empty")) {
                    GrassSquare g = new GrassSquare();
                    g.cut();
                    sharedState.setSquare(l, g);
                } else if (name.equals("puppy_empty")) {
                    GrassSquare g = new GrassSquare();
                    g.cut();
                    sharedState.setSquare(l, g);
                    sharedState.addPuppyLocation(l);
                } else if (name.equals("puppy_mower")) {
                    sharedState.addPuppyLocation(l);
                } else if (name.equals("mower")) {
                    // we already know other mowers' location => do nothing
                } else if ((name.equals("crater"))) {
                    sharedState.setSquare(l, new CraterSquare());
                } else if ((name.equals("fence"))) {
                    sharedState.setSquare(l, MowerSharedState.fence);
                }
            }

        } else if (feedback instanceof StallFeedback) {
            StallFeedback sf = (StallFeedback) feedback;
            int d = sf.getDistance();

            status = MowerStatus.Stalled;
            GrassSquare s = new GrassSquare();
            s.cut();

            if (d >= 1) {
                Location target = location.getMovedLocation(1, direction);
                sharedState.setSquare(target, s);
                location = target;
            }
            if (d == 2) {
                Location target = location.getMovedLocation(1, direction);
                sharedState.setSquare(target, s);
                location = target;
            }

            actionQueue.clear();
        } else if (feedback instanceof CrashFeedback) {
            status = MowerStatus.Crashed;
        } else if (feedback instanceof OKFeedback) {
            if (feedback.getAction() == null) {
                status = MowerStatus.TurnedOn;
            } else if (feedback.getAction() instanceof MoveAction) {
                MoveAction a = (MoveAction) feedback.getAction();
                GrassSquare s = new GrassSquare();
                s.cut();
                if (a.getMagnitude() > 1) {
                    Location midLocation = location.getMovedLocation(1, direction);
                    sharedState.setSquare(midLocation, s);
                }
                location = location.getMovedLocation(a.getMagnitude(), direction);
                sharedState.setSquare(location, s);

                direction = a.getNewDirection();

            } else if (feedback.getAction() instanceof TurnOffAction) {
                status = MowerStatus.TurnedOff;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mower mower = (Mower) o;
        return getId() == mower.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
