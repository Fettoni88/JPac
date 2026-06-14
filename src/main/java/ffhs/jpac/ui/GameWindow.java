package ffhs.jpac.ui;

import ffhs.jpac.engine.GameLoop;
import ffhs.jpac.maze.MazePosition;
import ffhs.jpac.world.*;

import javax.swing.JFrame;
import java.awt.Color;
import java.util.List;

public class GameWindow {

    private final JFrame frame;
    private final GameLoop loop;
    private final GamePanel panel;

    public GameWindow() {

        frame = new JFrame("JPac Prototype");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        TileMap map = new TileMap("/mazes/maze1.json");

        int worldWidth = map.getCols() * map.getTileSize();
        int worldHeight = map.getRows() * map.getTileSize();

        World world = new World(worldWidth, worldHeight, map);
        Player player = new Player(
                spawnX(map.getPlayerSpawn(), map, Player.SIZE),
                spawnY(map.getPlayerSpawn(), map, Player.SIZE)
        );
        world.addEntity(player);

        Color[] colors = {
                Color.RED,
                Color.PINK,
                Color.CYAN,
                Color.ORANGE
        };
        GhostPersonality[] personalities = {
                GhostPersonality.RED,
                GhostPersonality.PINK,
                GhostPersonality.CYAN,
                GhostPersonality.ORANGE
        };
        double[] releaseDelays = {0, 3, 6, 9};
        List<MazePosition> ghostSpawns = map.getGhostSpawns();

        for (int index = 0; index < 4; index++) {
            MazePosition spawn = ghostSpawns.get(index);
            world.addEntity(new Ghost(
                    spawnX(spawn, map, Ghost.SIZE),
                    spawnY(spawn, map, Ghost.SIZE),
                    colors[index],
                    personalities[index],
                    releaseDelays[index]
            ));
        }

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

    private double spawnX(
            MazePosition position,
            TileMap map,
            int entitySize
    ) {
        return position.col() * map.getTileSize()
                + map.getTileSize() / 2.0
                - entitySize / 2.0;
    }

    private double spawnY(
            MazePosition position,
            TileMap map,
            int entitySize
    ) {
        return position.row() * map.getTileSize()
                + map.getTileSize() / 2.0
                - entitySize / 2.0;
    }
}
