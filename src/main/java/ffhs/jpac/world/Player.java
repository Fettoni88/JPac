package ffhs.jpac.world;

public class Player extends MovingEntity {

    private static final int SIZE = 10;
    private Direction currentDirection = Direction.NONE;
    private Direction desiredDirection = Direction.NONE;

    public Player(double x, double y) {
        super(x, y, SIZE, 200.0);
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }

    public void setDesiredDirection(Direction direction) {
        this.desiredDirection = direction;
    }

    @Override
    public void update(World world, double deltaTime) {

        // Wenn gewünschte Richtung möglich ist, übernehmen
        if (world.canMove(this, desiredDirection)) {
            currentDirection = desiredDirection;
        }

        // Wenn aktuelle Richtung möglich ist, bewegen
        if (world.canMove(this, currentDirection)) {
            dx = currentDirection.getDx();
            dy = currentDirection.getDy();

            snapToGridCenter(world, deltaTime);
            move(world, deltaTime);
        } else {
            dx = 0;
            dy = 0;
        }
    }

    public void clampX(double min, double max) {
        if (x < min) x = min;
        if (x > max) x = max;
    }

    public void clampY(double min, double max) {
        if (y < min) y = min;
        if (y > max) y = max;
    }

    private void snapToGridCenter(World world, double deltaTime) {
        int tileSize = world.getMap().getTileSize();

        double centerX = x + size / 2.0;
        double centerY = y + size / 2.0;

        int tileCol = (int) (centerX / tileSize);
        int tileRow = (int) (centerY / tileSize);

        double targetX = tileCol * tileSize + tileSize / 2.0 - size / 2.0;
        double targetY = tileRow * tileSize + tileSize / 2.0 - size / 2.0;

        double snapSpeed = 3.0;

        if (currentDirection == Direction.LEFT || currentDirection == Direction.RIGHT) {
            y += (targetY - y) * snapSpeed * deltaTime;
        }

        if (currentDirection == Direction.UP || currentDirection == Direction.DOWN) {
            x += (targetX - x) * snapSpeed * deltaTime;
        }
    }
}