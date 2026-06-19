package ffhs.jpac.world;

/**
 * Definiert das zustandsabhängige Aktualisierungsverhalten eines Geistes.
 */
public interface GhostState {
    /**
     * Aktualisiert den Geist gemäss dem konkreten Zustand.
     *
     * @param ghost zu aktualisierender Geist
     * @param world aktuelle Spielwelt
     * @param deltaTime vergangene Zeit seit dem letzten Frame in Sekunden
     */
    void update(Ghost ghost, World world, double deltaTime);
}
