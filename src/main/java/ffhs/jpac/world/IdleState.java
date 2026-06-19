package ffhs.jpac.world;

/**
 * Zustand für eine richtungsstabile, nicht verfolgende Geistbewegung.
 */
public class IdleState implements GhostState {

    /**
     * Wählt bei Bedarf eine Leerlaufrichtung und bewegt den Geist.
     *
     * @param ghost zu aktualisierender Geist
     * @param world aktuelle Spielwelt
     * @param deltaTime vergangene Zeit seit dem letzten Frame in Sekunden
     */
    @Override
    public void update(Ghost ghost, World world, double deltaTime) {
        ghost.chooseIdleDirection(world);
        ghost.move(world, deltaTime);
    }
}
