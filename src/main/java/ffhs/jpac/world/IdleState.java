package ffhs.jpac.world;

public class IdleState implements GhostState {

    @Override
    public void update(Ghost ghost, World world, double deltaTime) {
        ghost.chooseIdleDirection(world);
        ghost.move(world, deltaTime);
    }
}
