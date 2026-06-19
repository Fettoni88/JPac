package ffhs.jpac.world;

/**
 * Kardinale Bewegungsrichtungen innerhalb des Kachelrasters.
 */
public enum Direction {

    /** Bewegung nach oben. */
    UP(0, -1),
    /** Bewegung nach unten. */
    DOWN(0, 1),
    /** Bewegung nach links. */
    LEFT(-1, 0),
    /** Bewegung nach rechts. */
    RIGHT(1, 0),
    /** Keine Bewegung. */
    NONE(0, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Gibt den horizontalen Richtungsanteil zurück.
     *
     * @return {@code -1}, {@code 0} oder {@code 1}
     */
    public int getDx() {
        return dx;
    }

    /**
     * Gibt den vertikalen Richtungsanteil zurück.
     *
     * @return {@code -1}, {@code 0} oder {@code 1}
     */
    public int getDy() {
        return dy;
    }

    /**
     * Ermittelt die entgegengesetzte Bewegungsrichtung.
     *
     * @return Gegenrichtung; für {@link #NONE} ebenfalls {@link #NONE}
     */
    public Direction opposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            case NONE -> NONE;
        };
    }
}
