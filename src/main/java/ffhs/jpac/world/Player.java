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
    public void reset() {
        super.reset();
        currentDirection = Direction.NONE;
        desiredDirection = Direction.NONE;
    }

    @Override
    public void update(World world, double deltaTime) {
        double movementDistance = Math.max(2, speed * deltaTime);
        boolean directionChanged = desiredDirection != currentDirection;
        boolean currentDirectionBlocked = currentDirection != Direction.NONE
                && !world.canMove(this, currentDirection, movementDistance);
        boolean canTurn = currentDirection == Direction.NONE
                || currentDirectionBlocked
                || isOppositeDirection(desiredDirection, currentDirection)
                || isCenteredOnTile(world, speed * deltaTime + 1);

        if (directionChanged
                && canTurn
                && canMoveFromTileCenter(
                        world,
                        desiredDirection,
                        movementDistance
                )) {
            currentDirection = desiredDirection;
        }

        if (world.canMove(this, currentDirection, movementDistance)) {
            dx = currentDirection.getDx();
            dy = currentDirection.getDy();
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

    private boolean isOppositeDirection(Direction first, Direction second) {
        return first.getDx() == -second.getDx()
                && first.getDy() == -second.getDy();
    }
}
