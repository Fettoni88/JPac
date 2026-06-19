package ffhs.jpac.world;

import ffhs.jpac.maze.MazePosition;

/**
 * Liefert das aktuelle Ziel einer Geistpersönlichkeit.
 */
interface GhostTargetStrategy {

    /**
     * Bestimmt die Zielkachel für die nächste Wegentscheidung.
     *
     * @param ghost Geist, für den das Ziel berechnet wird
     * @param world aktuelle Spielwelt
     * @return anzusteuernde Kachel
     */
    MazePosition getTarget(Ghost ghost, World world);

    /**
     * Aktualisiert zeitabhängige Strategieinformationen.
     *
     * @param deltaTime vergangene Zeit seit dem letzten Frame in Sekunden
     */
    default void update(double deltaTime) {
    }

    /**
     * Setzt den internen Strategiezustand für einen Neustart zurück.
     */
    default void reset() {
    }
}
