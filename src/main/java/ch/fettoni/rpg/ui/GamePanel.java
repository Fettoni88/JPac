package ch.fettoni.rpg.ui;

import ch.fettoni.rpg.world.Player;
import ch.fettoni.rpg.world.TileMap;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel {

    private final Player player;
    private final TileRenderer tileRenderer;

    public GamePanel(Player player, TileMap map) {
        this.player = player;
        this.tileRenderer = new TileRenderer(map);

        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_RIGHT -> player.setDx(1);
                    case KeyEvent.VK_LEFT -> player.setDx(-1);
                    case KeyEvent.VK_DOWN -> player.setDy(1);
                    case KeyEvent.VK_UP -> player.setDy(-1);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_RIGHT,
                         KeyEvent.VK_LEFT -> player.setDx(0);

                    case KeyEvent.VK_DOWN,
                         KeyEvent.VK_UP -> player.setDy(0);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        tileRenderer.render(g);

        g.setColor(Color.WHITE);
        g.fillRect(
                (int) player.getX(),
                (int) player.getY(),
                player.getSize(),
                player.getSize()
        );
    }

}