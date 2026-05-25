package ffhs.jpac.world;

public class IdleState implements GhostState {

    @Override
    public void update(Ghost ghost, World world, double deltaTime) {

        ghost.decreaseTimer(deltaTime);

        if (ghost.isTimerFinished()) {
            ghost.chooseRandomDirection();
            ghost.resetTimer();
        }

        ghost.move(world, deltaTime);
    }
}
