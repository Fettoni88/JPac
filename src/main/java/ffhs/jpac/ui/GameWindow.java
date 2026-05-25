package ffhs.jpac.ui;

import ffhs.jpac.engine.GameLoop;
import ffhs.jpac.world.*;


import javax.swing.JFrame;

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

        Player player = new Player(300, 300);
        Ghost ghost = new Ghost(200, 200);

        world.addEntity(player);
        world.addEntity(ghost);

        world.addPellet(new Pellet(100, 100));
        world.addPellet(new Pellet(150, 100));
        world.addPellet(new Pellet(200, 100));

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
