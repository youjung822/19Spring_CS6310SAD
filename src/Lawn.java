import java.util.Map;

public class Lawn {
    private int width;
    private int height;

    private Map<Location, Square> squares;

    public Lawn(int width, int height, Map<Location, Square> squares) {
        this.width = width;
        this.height = height;
        this.squares = squares;
    }

    public Map<Location, Square> getSquares() {
        return squares;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Square getSquare(Location location) {
        return squares.get(location);
    }

    public String[] scan(Location location) {
        Location[] neighbors = location.getNeighborLocations();
        String[] names = new String[8];
        for (int i = 0; i < 8; i++) {
            Location n = neighbors[i];
            if (n.getX() >= 0 && n.getX() < width && n.getY() >= 0 && n.getY() < height) {
                Square s = squares.get(n);
                names[i] = s.getName();
            } else {
                names[i] = "fence";
            }
        }
        return names;
    }

    public void cut(Location location) {
        ((GrassSquare) squares.get(location)).cut();
    }

    public boolean isGrass(Location location) {
        return location.getY() < width
                && location.getY() < height
                && squares.containsKey(location)
                && squares.get(location) instanceof GrassSquare;
    }

    public boolean isClean() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Square sq = squares.get(new Location(x, y));
                if (sq instanceof GrassSquare && !((GrassSquare) sq).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}