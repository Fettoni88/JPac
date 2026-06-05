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
        world.generatePellets();

        Player player = new Player(300, 300);

        Ghost redGhost = new Ghost(267, 235, Color.RED);
        Ghost pinkGhost = new Ghost(331, 235, Color.PINK);
        Ghost cyanGhost = new Ghost(395, 235, Color.CYAN);
        Ghost orangeGhost = new Ghost(459, 235, Color.ORANGE);

        world.addEntity(player);
        world.addEntity(redGhost);
        world.addEntity(pinkGhost);
        world.addEntity(cyanGhost);
        world.addEntity(orangeGhost);

        world.setPlayer(player);

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
