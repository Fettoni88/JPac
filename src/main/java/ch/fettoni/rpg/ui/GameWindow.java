package ch.fettoni.rpg.ui;

import ch.fettoni.rpg.engine.GameLoop;
import ch.fettoni.rpg.world.Player;
import ch.fettoni.rpg.world.TileMap;
import ch.fettoni.rpg.world.World;

import javax.swing.JFrame;

public class GameWindow {

    private final JFrame frame;
    private final GameLoop loop;
    private final GamePanel panel;

    public GameWindow() {

        frame = new JFrame("RPG – Prototyp");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        Player player = new Player(200, 200);
        TileMap map = new TileMap("/maps/map.txt");

        int worldWidth = map.getCols() * map.getTileSize();
        int worldHeight = map.getRows() * map.getTileSize();

        World world = new World(worldWidth, worldHeight, map);

        panel = new GamePanel(player, map);

        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);

        loop = new GameLoop(panel, player, world);
        loop.start();
    }

    public void show() {
        frame.setVisible(true);
        panel.requestFocusInWindow();
    }
}
