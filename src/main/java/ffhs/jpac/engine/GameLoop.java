package ffhs.jpac.engine;

import ffhs.jpac.ui.GamePanel;
import ffhs.jpac.world.World;

/**
 * Führt die zeitgesteuerte Aktualisierung der Spielwelt aus.
 *
 * <p>Die Schleife berechnet die seit dem letzten Frame vergangene Zeit,
 * aktualisiert die Welt und veranlasst anschliessend eine Neuzeichnung.</p>
 */
public class GameLoop implements Runnable {

    private static final int FPS = 60;

    private boolean running = false;
    private Thread thread;

    private final GamePanel panel;
    private final World world;

    /**
     * Erstellt eine Spielschleife für die angegebene Darstellung und Welt.
     *
     * @param panel darzustellendes Spielfeld
     * @param world zu aktualisierende Spielwelt
     */
    public GameLoop(GamePanel panel, World world) {
        this.panel = panel;
        this.world = world;
    }

    /**
     * Startet die Spielschleife in einem eigenen Thread.
     *
     * <p>Ein wiederholter Aufruf bleibt ohne Wirkung, solange die Schleife
     * bereits läuft.</p>
     */
    public void start() {
        if (running) return;

        running = true;
        thread = new Thread(this, "GameLoop-Thread");
        thread.start();
    }

    /**
     * Verarbeitet Frames, bis die Spielschleife beendet wird.
     *
     * <p>Die Aktualisierung ist zeitbasiert; die Schlafdauer gleicht die
     * restliche Zeit bis zum Zielwert von 60 Bildern pro Sekunde aus.</p>
     */
    @Override
    public void run() {

        final double targetTime = 1_000_000_000.0 / FPS;

        long lastTime = System.nanoTime();

        while (running) {

            long now = System.nanoTime();
            // Sekunden statt Frames bilden die Grundlage aller Bewegungen.
            double deltaTime = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;

            update(deltaTime);
            panel.repaint();

            long frameTime = System.nanoTime() - now;
            long sleepTime = (long) (targetTime - frameTime);

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1_000_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void update(double deltaTime) {
            world.update(deltaTime);
    }
}
