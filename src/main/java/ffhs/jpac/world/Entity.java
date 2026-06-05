package ffhs.jpac.world;

public abstract class Entity {

    protected double x;
    protected double y;
    protected int size;
    private final double spawnX;
    private final double spawnY;

    public Entity(double x, double y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.spawnX = x;
        this.spawnY = y;
    }

    public abstract void update(World world, double deltaTime);

    public void reset() {
        x = spawnX;
        y = spawnY;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}
