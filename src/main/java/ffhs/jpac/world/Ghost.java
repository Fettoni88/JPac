package ffhs.jpac.world;

import ffhs.jpac.maze.MazePosition;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Ghost extends MovingEntity {

    public static final int SIZE = 18;
    private static final double SPEED = 100.0;
    private static final double ATTACK_RANGE = 20;

    private final Color color;
    private final GhostPersonality personality;
    private final GhostTargetStrategy targetStrategy;
    private final double releaseDelaySeconds;
    private final Random random = new Random();
    private GhostState currentState = new IdleState();
    private double releaseElapsedSeconds;
    private int lastDecisionRow = -1;
    private int lastDecisionCol = -1;
    private GhostReleaseState releaseState =
            GhostReleaseState.WAITING_IN_HOUSE;

    public Ghost(double x, double y, Color color) {
        this(x, y, color, GhostPersonality.RED, 0);
    }

    public Ghost(
            double x,
            double y,
            Color color,
            GhostPersonality personality,
            double releaseDelaySeconds
    ) {
        super(x, y, SIZE, SPEED);
        this.color = color;
        this.personality = personality;
        this.targetStrategy = createTargetStrategy(personality);
        this.releaseDelaySeconds = releaseDelaySeconds;
    }

    public Color getColor() {
        return color;
    }

    public GhostPersonality getPersonality() {
        return personality;
    }

    public double getReleaseDelay() {
        return releaseDelaySeconds;
    }

    public boolean isReleased() {
        return releaseElapsedSeconds >= releaseDelaySeconds;
    }

    public boolean hasLeftGhostHouse() {
        return releaseState == GhostReleaseState.ACTIVE;
    }

    public boolean isActive() {
        return releaseState == GhostReleaseState.ACTIVE;
    }

    public GhostReleaseState getReleaseState() {
        return releaseState;
    }

    @Override
    public void reset() {
        super.reset();
        currentState = new IdleState();
        releaseElapsedSeconds = 0;
        lastDecisionRow = -1;
        lastDecisionCol = -1;
        releaseState = GhostReleaseState.WAITING_IN_HOUSE;
        targetStrategy.reset();
    }

    public void stopMoving() {
        dx = 0;
        dy = 0;
    }

    protected boolean chooseIdleDirection(World world) {
        if (!canChooseDirection(world)) {
            return false;
        }

        List<Direction> directions = getAvailableDirections(world);
        if (directions.isEmpty()) {
            stopMoving();
            return false;
        }

        Direction oppositeDirection = getDirection().opposite();
        List<Direction> forwardAndSideDirections = new ArrayList<>(
                directions
        );
        forwardAndSideDirections.remove(oppositeDirection);

        Direction nextDirection;
        if (forwardAndSideDirections.isEmpty()) {
            nextDirection = oppositeDirection;
        } else {
            Collections.shuffle(forwardAndSideDirections, random);
            nextDirection = forwardAndSideDirections.getFirst();
        }

        snapToTileCenter(world);
        setDirection(nextDirection);
        rememberDecisionTile(world);
        return true;
    }

    protected void chasePlayer(World world) {
        if (!canChooseDirection(world)) {
            return;
        }

        Player player = world.getPlayer();
        if (player == null) {
            stopMoving();
            return;
        }

        TileMap map = world.getMap();
        int row = map.getTileRowFromPixel(y + size / 2.0);
        int col = map.getTileColFromPixel(x + size / 2.0);
        MazePosition target = getChaseTarget(world);
        Direction direction = map.getDirectionTowardTarget(
                row,
                col,
                target.row(),
                target.col(),
                getDirection()
        );

        if (direction == Direction.NONE) {
            stopMoving();
            return;
        }

        snapToTileCenter(world);
        setDirection(direction);
        rememberDecisionTile(world);
    }

    MazePosition getChaseTarget(World world) {
        return targetStrategy.getTarget(this, world);
    }

    MazePosition getPlayerTile(World world) {
        TileMap map = world.getMap();
        Player player = world.getPlayer();
        int playerRow = map.getTileRowFromPixel(
                player.getY() + player.getSize() / 2.0
        );
        int playerCol = map.getTileColFromPixel(
                player.getX() + player.getSize() / 2.0
        );
        return new MazePosition(playerRow, playerCol);
    }

    MazePosition getCurrentTile(World world) {
        TileMap map = world.getMap();
        return new MazePosition(
                map.getTileRowFromPixel(y + size / 2.0),
                map.getTileColFromPixel(x + size / 2.0)
        );
    }

    @Override
    public void update(World world, double deltaTime) {
        if (releaseState == GhostReleaseState.WAITING_IN_HOUSE) {
            releaseElapsedSeconds += deltaTime;
            if (!isReleased()) {
                stopMoving();
                return;
            }

            releaseState = isInsideGhostHouse(world)
                    ? GhostReleaseState.LEAVING_HOUSE
                    : GhostReleaseState.ACTIVE;
        }

        if (releaseState == GhostReleaseState.LEAVING_HOUSE) {
            leaveGhostHouse(world, deltaTime);
            return;
        }

        targetStrategy.update(deltaTime);
        updateState(world);
        currentState.update(this, world, deltaTime);
    }

    private boolean isInsideGhostHouse(World world) {
        int row = world.getMap().getTileRowFromPixel(y + size / 2.0);
        int col = world.getMap().getTileColFromPixel(x + size / 2.0);
        return world.getMap().isInside(row, col)
                && world.getMap().isGhostHouse(row, col);
    }

    private void updateState(World world) {
        Player player = world.getPlayer();
        if (player == null) {
            currentState = new IdleState();
            return;
        }

        double distance = Math.hypot(player.getX() - x, player.getY() - y);
        if (distance < ATTACK_RANGE) {
            currentState = new AttackState();
        } else {
            currentState = new ChaseState();
        }
    }

    private GhostTargetStrategy createTargetStrategy(
            GhostPersonality personality
    ) {
        return switch (personality) {
            case RED -> new RedTargetStrategy();
            case PINK -> new PinkTargetStrategy();
            case BLUE -> new BlueTargetStrategy();
            case ORANGE -> new OrangeTargetStrategy();
        };
    }

    private boolean canChooseDirection(World world) {
        if (dx == 0 && dy == 0) {
            return true;
        }

        if (isCenteredOnTile(world, 2) && isOnNewDecisionTile(world)) {
            return true;
        }

        return false;
    }

    private List<Direction> getAvailableDirections(World world) {
        List<Direction> directions = new ArrayList<>();

        for (Direction direction : List.of(
                Direction.UP,
                Direction.DOWN,
                Direction.LEFT,
                Direction.RIGHT
        )) {
            if (canReachNextTile(world, direction)) {
                directions.add(direction);
            }
        }

        return directions;
    }

    Direction getDirection() {
        if (dx < 0) {
            return Direction.LEFT;
        }
        if (dx > 0) {
            return Direction.RIGHT;
        }
        if (dy < 0) {
            return Direction.UP;
        }
        if (dy > 0) {
            return Direction.DOWN;
        }
        return Direction.NONE;
    }

    void setDirection(Direction direction) {
        dx = direction.getDx();
        dy = direction.getDy();
    }

    private boolean canReachNextTile(World world, Direction direction) {
        return world.canMove(
                this,
                direction,
                world.getMap().getTileSize()
        );
    }

    private boolean isOnNewDecisionTile(World world) {
        int tileSize = world.getMap().getTileSize();
        int row = (int) ((y + size / 2.0) / tileSize);
        int col = (int) ((x + size / 2.0) / tileSize);
        return row != lastDecisionRow || col != lastDecisionCol;
    }

    private void rememberDecisionTile(World world) {
        int tileSize = world.getMap().getTileSize();
        lastDecisionRow = (int) ((y + size / 2.0) / tileSize);
        lastDecisionCol = (int) ((x + size / 2.0) / tileSize);
    }

    private void leaveGhostHouse(World world, double deltaTime) {
        int row = world.getMap().getTileRowFromPixel(y + size / 2.0);
        int col = world.getMap().getTileColFromPixel(x + size / 2.0);

        if (world.getMap().isGhostHouseExit(row, col)
                && isCenteredOnTile(world, 2)) {
            snapToTileCenter(world);
            releaseState = GhostReleaseState.ACTIVE;
            stopMoving();
            return;
        }

        if (canChooseDirection(world)) {
            Direction exitDirection =
                    world.getMap().getDirectionTowardNearestGhostHouseExit(
                            row,
                            col,
                            getDirection()
                    );
            if (exitDirection == Direction.NONE) {
                stopMoving();
                return;
            }

            snapToTileCenter(world);
            setDirection(exitDirection);
            rememberDecisionTile(world);
        }

        move(world, deltaTime);
    }
}
