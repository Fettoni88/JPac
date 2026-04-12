package ch.fettoni.rpg.ui;

import ch.fettoni.rpg.engine.GameLoop;
import ch.fettoni.rpg.world.NPC;
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

        TileMap map = new TileMap("/maps/map.txt");

        int worldWidth = map.getCols() * map.getTileSize();
        int worldHeight = map.getRows() * map.getTileSize();

        World world = new World(worldWidth, worldHeight, map);

        Player player = new Player(300, 300);
        NPC npc = new NPC(200, 200);

        world.addEntity(player);
        world.addEntity(npc);

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
