package ffhs.jpac.world;

public abstract class MovingEntity extends Entity {

    protected int dx = 0;
    protected int dy = 0;

    protected double speed;

    public MovingEntity(double x, double y, int size, double speed) {
        super(x, y, size);
        this.speed = speed;
    }

    @Override
    public void reset() {
        super.reset();
        dx = 0;
        dy = 0;
    }

    protected void move(World world, double deltaTime) {
        double moveX = dx;
        double moveY = dy;

        if (moveX != 0 || moveY != 0) {
            double length = Math.sqrt(moveX * moveX + moveY * moveY);
            moveX /= length;
            moveY /= length;
        }

        x += moveX * speed * deltaTime;
        if (world.isColliding(this)) {
            int tileSize = world.getMap().getTileSize();

            if (moveX > 0) {
                int tileX = (int) ((x + size) / tileSize);
                x = tileX * tileSize - size;
            } else if (moveX < 0) {
                int tileX = (int) (x / tileSize);
                x = (tileX + 1) * tileSize;
            }
        }

        y += moveY * speed * deltaTime;
        if (world.isColliding(this)) {
            int tileSize = world.getMap().getTileSize();

            if (moveY > 0) {
                int tileY = (int) ((y + size) / tileSize);
                y = tileY * tileSize - size;
            } else if (moveY < 0) {
                int tileY = (int) (y / tileSize);
                y = (tileY + 1) * tileSize;
            }
        }
    }
}
