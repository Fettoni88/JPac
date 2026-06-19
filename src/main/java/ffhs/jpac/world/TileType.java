package ffhs.jpac.world;

/**
 * Typ einer Kachel des Spielfelds.
 */
public enum TileType {

    /** Normal begehbarer Boden. */
    FLOOR(false),
    /** Boden des Geisterhauses mit besonderen Zugriffsregeln. */
    GHOST_HOUSE(false),
    /** Für alle Spielfiguren undurchdringliche Wand. */
    WALL(true);

    private final boolean solid;

    TileType(boolean solid) {
        this.solid = solid;
    }

    /**
     * Prüft, ob die Kachel grundsätzlich eine feste Wand darstellt.
     *
     * @return {@code true} für eine feste Kachel
     */
    public boolean isSolid() {
        return solid;
    }
}
