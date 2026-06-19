package ffhs.jpac.ui;

import ffhs.jpac.engine.GameLoop;
import ffhs.jpac.world.TileMap;
import ffhs.jpac.world.World;

import javax.swing.JFrame;

/**
 * Erstellt und verwaltet das nicht skalierbare Hauptfenster von JPac.
 *
 * <p>Die Klasse verbindet Karte, Spielwelt, Panel und Spielschleife.</p>
 */
public class GameWindow {

    private final JFrame frame;
    private final GameLoop loop;
    private final GamePanel panel;

    /**
     * Initialisiert das Fenster mit dem ersten Labyrinth und startet die
     * Spielschleife.
     */
    public GameWindow() {

        frame = new JFrame("JPac Prototype");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        TileMap map = new TileMap("/mazes/maze1.json");

        int worldWidth = map.getCols() * map.getTileSize();
        int worldHeight = map.getRows() * map.getTileSize();

        World world = new World(worldWidth, worldHeight, map);
        panel = new GamePanel(world);

        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);

        loop = new GameLoop(panel, world);
        loop.start();
    }

    /**
     * Zeigt das Fenster an und übergibt den Tastaturfokus an das Spielfeld.
     */
    public void show() {
        frame.setVisible(true);
        panel.requestFocus();
    }
}
