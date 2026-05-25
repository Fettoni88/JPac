package ffhs.jpac.world;

public class ChaseState implements GhostState {

    @Override
    public void update(Ghost ghost, World world, double deltaTime) {

        ghost.chasePlayer(world);
        ghost.move(world, deltaTime);
    }
}
