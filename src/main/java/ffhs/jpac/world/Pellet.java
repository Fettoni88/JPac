package ffhs.jpac.world;

public class Pellet extends Entity {

    public static final int SIZE = 5;
    private boolean collected = false;

    public Pellet(double x, double y) {
        super(x, y, SIZE);
    }

    @Override
    public void update(World world, double deltaTime) {
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }
}
