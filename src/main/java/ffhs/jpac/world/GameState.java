package ffhs.jpac.world;

/**
 * Zustände des übergeordneten Spielablaufs.
 */
public enum GameState {
    /** Hauptmenü. */
    START_MENU,
    /** Eingabe des Spielernamens. */
    NAME_INPUT,
    /** Auswahl eines Labyrinths. */
    MAZE_SELECTION,
    /** Laufende Spielrunde. */
    PLAYING,
    /** Anzeige der Highscore-Liste. */
    HIGHSCORE,
    /** Verlorene Spielrunde. */
    GAME_OVER,
    /** Gewonnene Spielrunde. */
    WIN
}
