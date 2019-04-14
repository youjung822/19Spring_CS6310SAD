import java.util.*;

public class MowerSharedState {
    public final static FenceSquare fence = new FenceSquare();
    private Map<Location, Square> squares;
    private List<Mower> mowers;
    private Set<Location> puppyLocations;
    private int puppyStayPercentage; //Assignment6 Page6

    public MowerSharedState() {
        squares = new HashMap<>();
        mowers = new ArrayList<>();
        puppyLocations = new HashSet<>();
    }

    public int getPuppyStayPercentage() {
        return puppyStayPercentage;
    }

    public void setPuppyStayPercentage(int puppyStayPercentage) {
        this.puppyStayPercentage = puppyStayPercentage;
    }

    public void expirePuppyLocations() {
        puppyLocations.clear();
    }

    public void addPuppyLocation(Location location) {
        puppyLocations.add(location);
    }

    public void setSquare(Location location, Square square) {
        squares.put(location, square);
        if (square instanceof FenceSquare) {
            int maxX = -1;
            int maxY = -1;
            Map<Integer, Integer> xCount = new HashMap<>();
            Map<Integer, Integer> yCount = new HashMap<>();

            for (Map.Entry<Location, Square> ls : squares.entrySet()) {
                if (ls.getValue() instanceof FenceSquare) {
                    Location l = ls.getKey();
                    if (l.getY() >= 0 && l.getX() >= 0) {
                        if (l.getY() > maxY) {
                            maxY = l.getY();
                        }
                        if (l.getX() > maxX) {
                            maxX = l.getX();
                        }
                        if (xCount.containsKey(l.getX())) {
                            xCount.put(l.getX(), xCount.get(l.getX()) + 1);
                        } else {
                            xCount.put(l.getX(), 1);
                        }

                        if (yCount.containsKey(l.getY())) {
                            yCount.put(l.getY(), yCount.get(l.getY()) + 1);
                        } else {
                            yCount.put(l.getY(), 1);
                        }
                    }
                }
            }

            if (maxX >= 0 && maxY >= 0) {
                int maxXCount = xCount.get(maxX);
                int maxYCount = yCount.get(maxY);

                if (maxXCount > 2 && maxYCount > 2) {
                    for (int y = -1; y <= maxY; y++) {
                        squares.put(new Location(maxX, y), fence);
                        squares.put(new Location(-1, y), fence);
                    }

                    for (int x = -1; x <= maxX; x++) {
                        squares.put(new Location(x, maxY), fence);
                        squares.put(new Location(x, -1), fence);
                    }
                }
            }
        }
    }

    public Map<Location, Square> getSquares() {
        return squares;
    }

    public boolean isSafeLocation(Location location) {
        if (puppyLocations.contains(location)) {
            return squares.containsKey(location)
                    && (squares.get(location) instanceof GrassSquare)
                    && !((GrassSquare) squares.get(location)).isEmpty();
        } else {
            for (Mower mower : mowers) {
                if (location.equals(mower.getLocation())) {
                    return false;
                }
            }
            return true;
        }
    }

    public void register(Mower mower) {
        mowers.add(mower);
        GrassSquare sq = new GrassSquare();
        sq.cut();
        setSquare(mower.getLocation(), sq);
    }

    public boolean isScanDone(Location location) {
        if (squares.containsKey(location) && squares.get(location) instanceof GrassSquare) {
            Location[] neighbors = location.getNeighborLocations();
            int found = 0;
            for (int i = 0; i < 8; i++) {
                Location neighbor = neighbors[i];
                if (squares.containsKey(neighbor)) {
                    found++;
                }
            }

            return found == 8;
        } else
            return true;
    }

    public boolean isWorkPending() {
        for (Map.Entry<Location, Square> ls : squares.entrySet()) {
            if (ls.getValue() instanceof GrassSquare) {
                GrassSquare grassSquare = (GrassSquare) ls.getValue();
                if (!(grassSquare.isEmpty() && isScanDone(ls.getKey()))) {
                    return true;
                }
            }
        }
        return false;
    }
}
