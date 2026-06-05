package ffhs.jpac.ui;

import ffhs.jpac.world.*;
import ffhs.jpac.world.Entity;
import ffhs.jpac.world.Player;
import ffhs.jpac.world.TileMap;
import ffhs.jpac.world.World;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel {

    private final Player player;
    private final TileRenderer tileRenderer;
    private final TileMap map;
    private final World world;
    private double cameraX = 0;
    private double cameraY = 0;

    public void updateInput() {
        // Input is handled via desiredDirection in keyPressed.
    }

    public GamePanel(Player player, TileMap map, World world) {
        this.player = player;
        this.world = world;
        this.map = map;
        this.tileRenderer = new TileRenderer(map);

        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER
                        && world.getGameState() == GameState.START) {
                    world.startGame();
                    return;
                }

                if (e.getKeyCode() == KeyEvent.VK_R
                        && (world.getGameState() == GameState.GAME_OVER
                        || world.getGameState() == GameState.WIN)) {
                    world.restartGame();
                    cameraX = 0;
                    cameraY = 0;
                    return;
                }

                if (world.getGameState() != GameState.RUNNING) {
                    return;
                }

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_RIGHT -> player.setDesiredDirection(Direction.RIGHT);
                    case KeyEvent.VK_LEFT  -> player.setDesiredDirection(Direction.LEFT);
                    case KeyEvent.VK_DOWN  -> player.setDesiredDirection(Direction.DOWN);
                    case KeyEvent.VK_UP    -> player.setDesiredDirection(Direction.UP);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Pacman keeps moving in the current direction.
            }
        });
    }

    private void renderStartScreen(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(g.getFont().deriveFont(48f));
        drawCentered(g, "JPac", 200);

        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(20f));
        drawCentered(g, "Press ENTER to Start", 280);
        drawCentered(g, "Controls: Arrow Keys", 320);
    }

    private void renderEndScreen(Graphics g, String title, boolean showFinalScore) {
        g.setColor(new Color(0, 0, 0, 210));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.YELLOW);
        g.setFont(g.getFont().deriveFont(42f));
        drawCentered(g, title, 240);

        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(22f));
        if (showFinalScore) {
            drawCentered(g, "Final Score: " + world.getScore(), 290);
        }
        drawCentered(g, "Press R to Restart", 330);
    }

    private void drawCentered(Graphics g, String text, int y) {
        int textWidth = g.getFontMetrics().stringWidth(text);
        int x = (getWidth() - textWidth) / 2;
        g.drawString(text, x, y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (world.getGameState() == GameState.START) {
            renderStartScreen(g);
            return;
        }

        // Kamera berechnen
        double targetX = player.getX() - getWidth() / 2.0;
        double targetY = player.getY() - getHeight() / 2.0;

        double smoothing = 0.05;

        cameraX += (targetX - cameraX) * smoothing;
        cameraY += (targetY - cameraY) * smoothing;

        int worldWidth = map.getCols() * map.getTileSize();
        int worldHeight = map.getRows() * map.getTileSize();

        double maxCameraX = Math.max(0, worldWidth - getWidth());
        double maxCameraY = Math.max(0, worldHeight - getHeight());

        cameraX = Math.max(0, cameraX);
        cameraY = Math.max(0, cameraY);

        cameraX = Math.min(maxCameraX, cameraX);
        cameraY = Math.min(maxCameraY, cameraY);

        int camX = (int) cameraX;
        int camY = (int) cameraY;

        // Tiles rendern
        tileRenderer.render(g, camX, camY, getWidth(), getHeight());

        // Pellets rendern
        g.setColor(Color.YELLOW);

        for (Pellet pellet : world.getPellets()) {
            if (!pellet.isCollected()) {
                int screenX = (int) pellet.getX() - camX;
                int screenY = (int) pellet.getY() - camY;

                g.fillOval(screenX, screenY, pellet.getSize(), pellet.getSize());
            }
        }

        // Entities rendern
        for (Entity e : world.getEntities()) {
            int screenX = (int) e.getX() - camX;
            int screenY = (int) e.getY() - camY;

            if (e instanceof Player) {
                g.setColor(Color.YELLOW);
            } else if (e instanceof Ghost ghost) {
                g.setColor(ghost.getColor());
            } else {
                g.setColor(Color.WHITE);
            }

            g.fillRect(screenX, screenY, e.getSize(), e.getSize());
        }

        // HUD / UI immer ganz am Schluss zeichnen
        g.setColor(Color.BLACK);
        g.fillRect(10, 5, 130, 25);

        g.setColor(Color.WHITE);
        g.drawString("Score: " + world.getScore(), 20, 22);

        if (world.getGameState() == GameState.WIN) {
            renderEndScreen(g, "YOU WIN!", true);
        } else if (world.getGameState() == GameState.GAME_OVER) {
            renderEndScreen(g, "GAME OVER", false);
        }
    }

}
