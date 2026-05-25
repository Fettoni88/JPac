package ffhs.jpac.world;

public class Player extends MovingEntity {

    private static final int SIZE = 10;

    public Player(double x, double y) {
        super(x, y, SIZE, 200.0);
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }

    @Override
    public void update(World world, double deltaTime) {
        move(world, deltaTime);

        clampX(0, world.getWidth() - size);
        clampY(0, world.getHeight() - size);
    }

    public void clampX(double min, double max) {
        if (x < min) x = min;
        if (x > max) x = max;
    }

    public void clampY(double min, double max) {
        if (y < min) y = min;
        if (y > max) y = max;
    }
}