package ffhs.jpac.ui;

import ch.fettoni.rpg.world.*;
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

    private boolean upPressed, downPressed, leftPressed, rightPressed;

    public void updateInput() {
        int dx = (rightPressed ? 1 : 0) - (leftPressed ? 1 : 0);
        int dy = (downPressed ? 1 : 0) - (upPressed ? 1 : 0);

        player.setDx(dx);
        player.setDy(dy);
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
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_RIGHT -> rightPressed = true;
                    case KeyEvent.VK_LEFT  -> leftPressed = true;
                    case KeyEvent.VK_DOWN  -> downPressed = true;
                    case KeyEvent.VK_UP    -> upPressed = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_RIGHT -> rightPressed = false;
                    case KeyEvent.VK_LEFT  -> leftPressed = false;
                    case KeyEvent.VK_DOWN  -> downPressed = false;
                    case KeyEvent.VK_UP    -> upPressed = false;
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

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

        tileRenderer.render(g, camX, camY, getWidth(), getHeight());

        for (Entity e : world.getEntities()) {
            int screenX = (int) e.getX() - camX;
            int screenY = (int) e.getY() - camY;

            if (e instanceof Player) {
                g.setColor(Color.WHITE);
            } else {
                g.setColor(Color.RED);
            }

            g.fillRect(screenX, screenY, e.getSize(), e.getSize());
        }
    }

}