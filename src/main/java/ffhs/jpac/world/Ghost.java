package ffhs.jpac.world;

import java.awt.Color;
import java.util.Random;

public class Ghost extends MovingEntity {

    private static final int SIZE = 10;
    private static final double SPEED = 100.0;
    private final Color color;
    private GhostState currentState;

    private double directionTimer = 0;
    private final double directionInterval = 2.0;

    private final double chaseRange = 150;
    private final double attackRange = 20;

    private final Random random = new Random();

    public Ghost(double x, double y, Color color) {
        super(x, y, SIZE, SPEED);
        this.color = color;
        currentState = new IdleState();
    }

    public Color getColor() {
        return color;
    }

    @Override
    public void reset() {
        super.reset();
        currentState = new IdleState();
        directionTimer = 0;
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

    private void updateState(World world) {
        Player player = world.getPlayer();
        if (player == null) {
            currentState = new IdleState();
            return;
        }

        double diffX = player.getX() - x;
        double diffY = player.getY() - y;

        double distance = Math.sqrt(diffX * diffX + diffY * diffY);

        if (distance < attackRange) {
            if (!(currentState instanceof AttackState)) {
                currentState = new AttackState();
            }
        } else if (distance < chaseRange) {
            if (!(currentState instanceof ChaseState)) {
                currentState = new ChaseState();
            }
        } else {
            if (!(currentState instanceof IdleState)) {
                currentState = new IdleState();
            }
        }
    }

    protected void chooseRandomDirection() {
        dx = random.nextInt(3) - 1;
        dy = random.nextInt(3) - 1;

        if (dx == 0 && dy == 0) {
            dx = 1;
        }
    }

    protected void chasePlayer(World world) {
        Player player = world.getPlayer();

        double diffX = player.getX() - x;
        double diffY = player.getY() - y;

        double distance = Math.sqrt(diffX * diffX + diffY * diffY);

        double stopDistance = 10;

        if (distance < stopDistance) {
            dx = 0;
            dy = 0;
            return;
        }

        dx = 0;
        dy = 0;

        if (Math.abs(diffX) > Math.abs(diffY)) {
            dx = diffX > 0 ? 1 : -1;
        } else {
            dy = diffY > 0 ? 1 : -1;
        }
    }

    @Override
    public void update(World world, double deltaTime) {

        updateState(world);

        currentState.update(this, world, deltaTime);
    }
}
