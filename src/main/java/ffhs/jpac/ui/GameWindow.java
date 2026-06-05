package ffhs.jpac.ui;

import ffhs.jpac.engine.GameLoop;
import ffhs.jpac.world.*;


import javax.swing.JFrame;
import java.awt.Color;

public class GameWindow {

    private final JFrame frame;
    private final GameLoop loop;
    private final GamePanel panel;

    public GameWindow() {

        frame = new JFrame("JPac Prototype");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        TileMap map = new TileMap("/maps/map.txt");

        int worldWidth = map.getCols() * map.getTileSize();
        int worldHeight = map.getRows() * map.getTileSize();

        World world = new World(worldWidth, worldHeight, map);
        Player player = new Player(331, 427);

        Ghost redGhost = new Ghost(
                299, 267, Color.RED, GhostPersonality.RED, 0
        );
        Ghost pinkGhost = new Ghost(
                331, 267, Color.PINK, GhostPersonality.PINK, 3
        );
        Ghost cyanGhost = new Ghost(
                363, 267, Color.CYAN, GhostPersonality.CYAN, 6
        );
        Ghost orangeGhost = new Ghost(
                331, 299, Color.ORANGE, GhostPersonality.ORANGE, 9
        );

        world.addEntity(player);
        world.addEntity(redGhost);
        world.addEntity(pinkGhost);
        world.addEntity(cyanGhost);
        world.addEntity(orangeGhost);

        world.setPlayer(player);
        world.generatePellets();

        panel = new GamePanel(player, map, world);

        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);

        loop = new GameLoop(panel, world);
        loop.start();
    }

    public void show() {
        frame.setVisible(true);
        panel.requestFocus();
    }
}
