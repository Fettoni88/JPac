package ffhs.jpac.world;

/**
 * Einsammelbarer Punktgegenstand im Labyrinth.
 */
public class Pellet extends Entity {

    /** Quadratische Kantenlänge eines Pellets in Pixeln. */
    public static final int SIZE = 5;
    private boolean collected = false;

    /**
     * Erstellt ein Pellet an der angegebenen Pixelposition.
     *
     * @param x horizontale Position
     * @param y vertikale Position
     */
    public Pellet(double x, double y) {
        super(x, y, SIZE);
    }

    /**
     * Aktualisiert das Pellet.
     *
     * <p>Pellets besitzen keinen zeitabhängigen Zustand; die Methode erfüllt
     * den Vertrag der Basisklasse.</p>
     *
     * @param world aktuelle Spielwelt
     * @param deltaTime vergangene Zeit seit dem letzten Frame in Sekunden
     */
    @Override
    public void update(World world, double deltaTime) {
    }

    /**
     * Prüft, ob das Pellet bereits eingesammelt wurde.
     *
     * @return {@code true}, wenn es eingesammelt ist
     */
    public boolean isCollected() {
        return collected;
    }

    /**
     * Markiert das Pellet als eingesammelt.
     */
    public void collect() {
        collected = true;
    }
}
