package ch.fettoni.rpg.world;

public class NPC extends MovingEntity {

    private static final int SIZE = 10;
    private static final double SPEED = 100.0;
    private NPCState currentState;

    private double directionTimer = 0;
    private double directionInterval = 2.0;

    private double chaseRange = 150;
    private double attackRange = 20;

    private java.util.Random random = new java.util.Random();

    public NPC(double x, double y) {
        super(x, y, SIZE, SPEED);
        currentState = new IdleState();
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

        double diffX = player.getX() - x;
        double diffY = player.getY() - y;

        double distance = Math.sqrt(diffX * diffX + diffY * diffY);

        if (distance < attackRange) {
            if (!(currentState instanceof AttackState)) {
                currentState = new AttackState();
            }
        }
        else if (distance < chaseRange) {
            if (!(currentState instanceof ChaseState)) {
                currentState = new ChaseState();
            }
        }
        else {
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