import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MowerSharedState {
    private Map<Location, Square> squares;
    private List<Mower> mowers;

    public MowerSharedState() {
        squares = new HashMap<>();
        mowers = new ArrayList<>();
    }

    public void setSquare(Location location, Square square) {
        squares.put(location, square);
    }

    public Map<Location, Square> getSquares() {
        return squares;
    }

    public boolean isMower(Location location) {
        for (Mower mower : mowers) {
            if (location.equals(mower.getLocation())) {
                return true;
            }
        }
        return false;
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
