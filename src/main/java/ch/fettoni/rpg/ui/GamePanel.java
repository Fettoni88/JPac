package ch.fettoni.rpg.ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import ch.fettoni.rpg.world.Player;


public class GamePanel extends JPanel {

    private final Player player;

    public GamePanel(Player player) {
        this.player = player;

        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.WHITE);
        g.fillRect(
                player.getX(),
                player.getY(),
                player.getSize(),
                player.getSize()
        );
    }
}