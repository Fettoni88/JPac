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
        move(world, deltaTime, true);
    }

    protected void move(World world, double deltaTime, boolean lockToLane) {
        keepCardinalDirection();
        if (lockToLane) {
            centerOnLane(world, deltaTime);
        }

        double moveX = dx;
        double moveY = dy;

        double oldX = x;
        x += moveX * speed * deltaTime;
        if (world.isColliding(this)) {
            x = oldX;
            dx = 0;
        }

        double oldY = y;
        y += moveY * speed * deltaTime;
        if (world.isColliding(this)) {
            y = oldY;
            dy = 0;
        }
    }

    protected boolean isCenteredOnTile(World world, double tolerance) {
        int tileSize = world.getMap().getTileSize();
        double centerX = x + size / 2.0;
        double centerY = y + size / 2.0;
        double tileCenterX = nearestTileCenter(centerX, tileSize);
        double tileCenterY = nearestTileCenter(centerY, tileSize);

        return Math.abs(centerX - tileCenterX) <= tolerance
                && Math.abs(centerY - tileCenterY) <= tolerance;
    }

    protected void snapToTileCenter(World world) {
        int tileSize = world.getMap().getTileSize();
        x = nearestTileCenter(x + size / 2.0, tileSize) - size / 2.0;
        y = nearestTileCenter(y + size / 2.0, tileSize) - size / 2.0;
    }

    protected boolean canMoveFromTileCenter(World world, Direction direction) {
        return canMoveFromTileCenter(world, direction, 2);
    }

    protected boolean canMoveFromTileCenter(
            World world,
            Direction direction,
            double distance
    ) {
        double oldX = x;
        double oldY = y;

        snapToTileCenter(world);
        boolean canMove = world.canMove(this, direction, distance);

        x = oldX;
        y = oldY;
        return canMove;
    }

    private void centerOnLane(World world, double deltaTime) {
        int tileSize = world.getMap().getTileSize();
        double correction = speed * deltaTime * 0.5;

        if (dx != 0) {
            double targetY = nearestTileCenter(y + size / 2.0, tileSize) - size / 2.0;
            y = moveToward(y, targetY, correction);
        } else if (dy != 0) {
            double targetX = nearestTileCenter(x + size / 2.0, tileSize) - size / 2.0;
            x = moveToward(x, targetX, correction);
        }
    }

    private double nearestTileCenter(double center, int tileSize) {
        return Math.round((center - tileSize / 2.0) / tileSize)
                * tileSize + tileSize / 2.0;
    }

    private double moveToward(double current, double target, double maxDistance) {
        double difference = target - current;

        if (Math.abs(difference) <= maxDistance) {
            return target;
        }

        return current + Math.signum(difference) * maxDistance;
    }

    private void keepCardinalDirection() {
        if (dx != 0) {
            dx = Integer.signum(dx);
            dy = 0;
        } else if (dy != 0) {
            dy = Integer.signum(dy);
        }
    }
}
