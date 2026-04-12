package ch.fettoni.rpg.engine;

import ch.fettoni.rpg.ui.GamePanel;
import ch.fettoni.rpg.world.Player;
import ch.fettoni.rpg.world.World;

public class GameLoop implements Runnable {

    private static final int FPS = 60;

    private boolean running = false;
    private Thread thread;

    private final GamePanel panel;
    private final World world;

    public GameLoop(GamePanel panel, World world) {
        this.panel = panel;
        this.world = world;
    }

    public void start() {
        if (running) return;

        running = true;
        thread = new Thread(this, "GameLoop-Thread");
        thread.start();
    }

    @Override
    public void run() {

        final double targetTime = 1_000_000_000.0 / FPS;

        long lastTime = System.nanoTime();

        while (running) {

            long now = System.nanoTime();
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
        panel.updateInput();
        world.update(deltaTime);
    }
}