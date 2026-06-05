package ffhs.jpac.ui;

import ffhs.jpac.world.GameState;
import ffhs.jpac.world.Ghost;
import ffhs.jpac.world.Pellet;
import ffhs.jpac.world.Player;
import ffhs.jpac.world.TileMap;
import ffhs.jpac.world.World;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GamePanelTest {

    @Test
    void enterKeyStartsGame() {
        TileMap map = new TileMap("/maps/map.txt");
        World world = new World(
                map.getCols() * map.getTileSize(),
                map.getRows() * map.getTileSize(),
                map
        );
        Player player = new Player(43, 43);
        world.setPlayer(player);
        world.addEntity(player);

        GamePanel panel = new GamePanel(player, map, world);
        pressKey(panel, KeyEvent.VK_ENTER, '\n');

        assertEquals(GameState.RUNNING, world.getGameState());
    }

    @Test
    void rKeyRestartsFinishedGame() {
        TileMap map = new TileMap("/maps/map.txt");
        World world = new World(
                map.getCols() * map.getTileSize(),
                map.getRows() * map.getTileSize(),
                map
        );
        Player player = new Player(43, 43);
        Ghost ghost = new Ghost(43, 43, Color.RED);
        world.setPlayer(player);
        world.addEntity(player);
        world.addEntity(ghost);
        world.addPellet(new Pellet(75, 43));
        world.startGame();
        world.update(0.0);

        GamePanel panel = new GamePanel(player, map, world);
        pressKey(panel, KeyEvent.VK_R, 'R');

        assertEquals(GameState.START, world.getGameState());
        assertEquals(0, world.getScore());
    }

    private void pressKey(GamePanel panel, int keyCode, char keyChar) {
        KeyEvent event = new KeyEvent(
                panel,
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                0,
                keyCode,
                keyChar
        );

        for (KeyListener listener : panel.getKeyListeners()) {
            listener.keyPressed(event);
        }
    }
}
