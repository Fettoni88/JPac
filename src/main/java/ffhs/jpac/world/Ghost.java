package ffhs.jpac.world;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Ghost extends MovingEntity {

    private static final int SIZE = 10;
    private static final double SPEED = 100.0;
    private static final double ATTACK_RANGE = 20;
    private static final double ORANGE_CHASE_RANGE = 150;

    private final Color color;
    private final GhostPersonality personality;
    private final double releaseDelay;
    private final Random random = new Random();
    private GhostState currentState = new IdleState();
    private double releaseTimer = 0;
    private double directionTimer = 0;
    private final double directionInterval = 2.0;
    private int lastDecisionRow = -1;
    private int lastDecisionCol = -1;
    private boolean leftGhostHouse = false;

    public Ghost(double x, double y, Color color) {
        this(x, y, color, GhostPersonality.RED, 0);
    }

    public Ghost(
            double x,
            double y,
            Color color,
            GhostPersonality personality,
            double releaseDelay
    ) {
        super(x, y, SIZE, SPEED);
        this.color = color;
        this.personality = personality;
        this.releaseDelay = releaseDelay;
    }

    public Color getColor() {
        return color;
    }

    public GhostPersonality getPersonality() {
        return personality;
    }

    public boolean isReleased() {
        return releaseTimer >= releaseDelay;
    }

    public boolean hasLeftGhostHouse() {
        return leftGhostHouse;
    }

    @Override
    public void reset() {
        super.reset();
        currentState = new IdleState();
        releaseTimer = 0;
        directionTimer = 0;
        lastDecisionRow = -1;
        lastDecisionCol = -1;
        leftGhostHouse = false;
    }

    public void decreaseTimer(double deltaTime) {
        directionTimer -= deltaTime;
    }

    public boolean isTimerFinished() {
        return directionTimer <= 0;
    }

    public void resetTimer() {
        directionTimer = directionInterval;
    }

    public void stopMoving() {
        dx = 0;
        dy = 0;
    }

    protected boolean chooseRandomDirection(World world) {
        if (!canChooseDirection(world)) {
            return false;
        }

        List<Direction> directions = getAvailableDirections(world);
        if (directions.isEmpty()) {
            stopMoving();
            return false;
        }

        Collections.shuffle(directions, random);
        setDirection(directions.get(0));
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

        double diffX = player.getX() - x;
        double diffY = player.getY() - y;
        List<Direction> preferredDirections = new ArrayList<>();

        if (personality == GhostPersonality.PINK) {
            addVerticalDirection(preferredDirections, diffY);
            addHorizontalDirection(preferredDirections, diffX);
        } else if (Math.abs(diffX) >= Math.abs(diffY)) {
            addHorizontalDirection(preferredDirections, diffX);
            addVerticalDirection(preferredDirections, diffY);
        } else {
            addVerticalDirection(preferredDirections, diffY);
            addHorizontalDirection(preferredDirections, diffX);
        }

        addFallbackDirections(preferredDirections);

        for (Direction direction : preferredDirections) {
            if (canReachNextTile(world, direction)) {
                setDirection(direction);
                rememberDecisionTile(world);
                return;
            }
        }

        stopMoving();
    }

    @Override
    public void update(World world, double deltaTime) {
        if (!isReleased()) {
            releaseTimer += deltaTime;
            stopMoving();
            return;
        }

        if (!leftGhostHouse) {
            if (isInsideGhostHouse(world)) {
                moveTowardGhostHouseExit(world, deltaTime);
                return;
            }

            leftGhostHouse = true;
            stopMoving();
        }

        updateState(world);
        currentState.update(this, world, deltaTime);
    }

    private void updateState(World world) {
        Player player = world.getPlayer();
        if (player == null) {
            currentState = new IdleState();
            return;
        }

        double diffX = player.getX() - x;
        double diffY = player.getY() - y;
        double distance = Math.sqrt(diffX * diffX + diffY * diffY);

        if (distance < ATTACK_RANGE) {
            currentState = new AttackState();
        } else if (shouldChase(distance)) {
            currentState = new ChaseState();
        } else {
            currentState = new IdleState();
        }
    }

    private boolean shouldChase(double distance) {
        return switch (personality) {
            case RED, PINK -> true;
            case CYAN -> false;
            case ORANGE -> distance < ORANGE_CHASE_RANGE;
        };
    }

    private boolean canChooseDirection(World world) {
        if (dx == 0 && dy == 0) {
            snapToTileCenter(world);
            return true;
        }

        if (isCenteredOnTile(world, 2) && isOnNewDecisionTile(world)) {
            snapToTileCenter(world);
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

    private void addHorizontalDirection(List<Direction> directions, double diffX) {
        if (diffX != 0) {
            directions.add(diffX > 0 ? Direction.RIGHT : Direction.LEFT);
        }
    }

    private void addVerticalDirection(List<Direction> directions, double diffY) {
        if (diffY != 0) {
            directions.add(diffY > 0 ? Direction.DOWN : Direction.UP);
        }
    }

    private void addFallbackDirections(List<Direction> directions) {
        for (Direction direction : List.of(
                Direction.UP,
                Direction.DOWN,
                Direction.LEFT,
                Direction.RIGHT
        )) {
            if (!directions.contains(direction)) {
                directions.add(direction);
            }
        }
    }

    private void setDirection(Direction direction) {
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

    private boolean isInsideGhostHouse(World world) {
        int tileSize = world.getMap().getTileSize();
        int row = (int) ((y + size / 2.0) / tileSize);
        int col = (int) ((x + size / 2.0) / tileSize);
        return world.getMap().isGhostHouse(row, col);
    }

    private void moveTowardGhostHouseExit(World world, double deltaTime) {
        int tileSize = world.getMap().getTileSize();
        int[] exit = world.getMap().findGhostHouseExit();
        double exitX = exit[3] * tileSize + tileSize / 2.0 - size / 2.0;
        double exitY = exit[2] * tileSize + tileSize / 2.0 - size / 2.0;
        double step = speed * deltaTime;

        if (Math.abs(x - exitX) > step) {
            setDirection(x < exitX ? Direction.RIGHT : Direction.LEFT);
            move(world, deltaTime);
            return;
        }

        x = exitX;

        if (Math.abs(y - exitY) > step) {
            setDirection(y < exitY ? Direction.DOWN : Direction.UP);
            move(world, deltaTime);
            return;
        }

        y = exitY;
        leftGhostHouse = true;
        stopMoving();
    }
}
