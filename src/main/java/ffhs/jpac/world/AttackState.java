package ffhs.jpac.world;

public class AttackState implements GhostState {

    @Override
    public void update(Ghost ghost, World world, double deltaTime) {

        ghost.stopMoving();

        // Debug (später Animation / Damage)
        System.out.println("Ghost greift an!");
    }
}
