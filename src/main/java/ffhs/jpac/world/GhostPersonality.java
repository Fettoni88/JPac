package ffhs.jpac.world;

/**
 * Unterscheidet die vier Zielstrategien der Geister.
 */
public enum GhostPersonality {
    /** Direkter Verfolger des Spielers. */
    RED,
    /** Berechnet einen Vorhaltepunkt vor dem Spieler. */
    PINK,
    /** Wechselt zwischen Patrouille und zeitweiliger Verfolgung. */
    BLUE,
    /** Verfolgt aus der Distanz und flieht im Nahbereich. */
    ORANGE
}
