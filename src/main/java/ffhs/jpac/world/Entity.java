package ffhs.jpac.world;

/**
 * Abstrakte Basis aller positionierten Objekte der Spielwelt.
 */
public abstract class Entity {

    /** Horizontale Pixelposition der linken oberen Ecke. */
    protected double x;
    /** Vertikale Pixelposition der linken oberen Ecke. */
    protected double y;
    /** Quadratische Kantenlänge der Entität in Pixeln. */
    protected int size;
    private final double spawnX;
    private final double spawnY;

    /**
     * Erstellt eine Entität und merkt ihre Position als Spawnpunkt.
     *
     * @param x horizontale Pixelposition der linken oberen Ecke
     * @param y vertikale Pixelposition der linken oberen Ecke
     * @param size quadratische Kantenlänge in Pixeln
     */
    public Entity(double x, double y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.spawnX = x;
        this.spawnY = y;
    }

    /**
     * Aktualisiert die Entität für einen Frame.
     *
     * @param world aktuelle Spielwelt
     * @param deltaTime vergangene Zeit seit dem letzten Frame in Sekunden
     */
    public abstract void update(World world, double deltaTime);

    /**
     * Setzt die Entität an ihren ursprünglichen Spawnpunkt zurück.
     */
    public void reset() {
        x = spawnX;
        y = spawnY;
    }

    /**
     * Gibt die horizontale Pixelposition zurück.
     *
     * @return horizontale Pixelposition
     */
    public double getX() {
        return x;
    }

    /**
     * Gibt die vertikale Pixelposition zurück.
     *
     * @return vertikale Pixelposition
     */
    public double getY() {
        return y;
    }

    /**
     * Gibt die quadratische Kantenlänge zurück.
     *
     * @return Kantenlänge in Pixeln
     */
    public int getSize() {
        return size;
    }

    /**
     * Setzt die horizontale Pixelposition.
     *
     * @param x neue horizontale Pixelposition
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Setzt die vertikale Pixelposition.
     *
     * @param y neue vertikale Pixelposition
     */
    public void setY(double y) {
        this.y = y;
    }
}
