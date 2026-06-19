package ffhs.jpac.world;

/**
 * Zustand für die zielgerichtete Bewegung eines aktiven Geistes.
 */
public class ChaseState implements GhostState {

    /**
     * Lässt den Geist ein Persönlichkeitsziel verfolgen und bewegt ihn.
     *
     * @param ghost zu aktualisierender Geist
     * @param world aktuelle Spielwelt
     * @param deltaTime vergangene Zeit seit dem letzten Frame in Sekunden
     */
    @Override
    public void update(Ghost ghost, World world, double deltaTime) {

        ghost.chasePlayer(world);
        ghost.move(world, deltaTime);
    }
}
