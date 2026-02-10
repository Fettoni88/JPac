package ch.fettoni.rpg.engine;

import ch.fettoni.rpg.ui.GamePanel;
import ch.fettoni.rpg.world.Player;
import ch.fettoni.rpg.world.World;

public class GameLoop implements Runnable {

    private static final int FPS = 60;
    private boolean running = false;
    private Thread thread;

    private final GamePanel panel;
    private final Player player;
    private final World world;


    public GameLoop(GamePanel panel, Player player, World world) {
        this.panel = panel;
        this.player = player;
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
        long frameTime = 1000 / FPS;

        while (running) {
            long startTime = System.currentTimeMillis();

            update();
            panel.repaint(); // 🔑 DAS ist der Schlüssel

            long elapsed = System.currentTimeMillis() - startTime;
            long sleepTime = frameTime - elapsed;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void update() {
        world.update(player);
    }
}