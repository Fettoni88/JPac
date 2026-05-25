package ffhs.jpac.world;

public class Pellet extends Entity {

    private static final int SIZE = 6;
    private boolean collected = false;

    public Pellet(double x, double y) {
        super(x, y, SIZE);
    }

    @Override
    public void update(World world, double deltaTime) {
        // Pellets bewegen sich nicht
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }
}