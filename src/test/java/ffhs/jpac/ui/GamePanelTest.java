package ffhs.jpac.ui;

import ffhs.jpac.persistence.HighscoreManager;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GamePanelTest {

    @Test
    void startOptionOpensNameInputAndEnterStartsGame() {
        TestGame testGame = createTestGame(43, 43);

        pressKey(testGame.panel(), KeyEvent.VK_ENTER, '\n');
        assertEquals(GameState.NAME_INPUT, testGame.world().getGameState());

        pressKey(testGame.panel(), KeyEvent.VK_N, 'N');
        pressKey(testGame.panel(), KeyEvent.VK_E, 'e');
        pressKey(testGame.panel(), KeyEvent.VK_C, 'c');
        pressKey(testGame.panel(), KeyEvent.VK_I, 'i');
        pressKey(testGame.panel(), KeyEvent.VK_B, 'b');
        pressKey(testGame.panel(), KeyEvent.VK_ENTER, '\n');

        assertEquals(GameState.PLAYING, testGame.world().getGameState());
        assertEquals("Necib", testGame.world().getPlayerName());
    }

    @Test
    void emptyNameUsesPlayerAsDefault() {
        TestGame testGame = createTestGame(43, 43);

        pressKey(testGame.panel(), KeyEvent.VK_ENTER, '\n');
        pressKey(testGame.panel(), KeyEvent.VK_ENTER, '\n');

        assertEquals(GameState.PLAYING, testGame.world().getGameState());
        assertEquals("Player", testGame.world().getPlayerName());
    }

    @Test
    void keyboardOpensHighscoresAndEscapeReturnsToMenu() {
        TestGame testGame = createTestGame(43, 43);

        pressKey(testGame.panel(), KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED);
        pressKey(testGame.panel(), KeyEvent.VK_ENTER, '\n');

        assertEquals(GameState.HIGHSCORE, testGame.world().getGameState());

        pressKey(
                testGame.panel(),
                KeyEvent.VK_ESCAPE,
                KeyEvent.CHAR_UNDEFINED
        );

        assertEquals(GameState.START_MENU, testGame.world().getGameState());
    }

    @Test
    void mouseHoverAndClickActivateHighscoreOption() {
        TestGame testGame = createTestGame(43, 43);

        moveMouse(testGame.panel(), 400, 330);
        clickMouse(testGame.panel(), 400, 330);

        assertEquals(GameState.HIGHSCORE, testGame.world().getGameState());
    }

    @Test
    void rKeyReturnsFinishedGameToMainMenu() {
        TestGame testGame = createTestGame(43, 43);
        Ghost ghost = new Ghost(43, 43, Color.RED);
        testGame.world().addEntity(ghost);
        testGame.world().addPellet(new Pellet(75, 43));
        testGame.world().startGame("Player");
        testGame.world().update(0.0);

        pressKey(testGame.panel(), KeyEvent.VK_R, 'R');

        assertEquals(GameState.START_MENU, testGame.world().getGameState());
        assertEquals(0, testGame.world().getScore());
    }

    @Test
    void arrowKeyMovesPlayerOnlyAfterNameIsConfirmed() {
        TestGame testGame = createTestGame(331, 427);
        testGame.world().generatePellets();

        pressKey(
                testGame.panel(),
                KeyEvent.VK_RIGHT,
                KeyEvent.CHAR_UNDEFINED
        );
        testGame.world().update(1.0);
        assertEquals(331, testGame.player().getX(), 0.0001);

        pressKey(testGame.panel(), KeyEvent.VK_ENTER, '\n');
        pressKey(testGame.panel(), KeyEvent.VK_ENTER, '\n');
        pressKey(
                testGame.panel(),
                KeyEvent.VK_RIGHT,
                KeyEvent.CHAR_UNDEFINED
        );

        for (int frame = 0; frame < 10; frame++) {
            testGame.world().update(1.0 / 60.0);
        }

        assertTrue(testGame.player().getX() > 350);
        assertEquals(427, testGame.player().getY(), 0.0001);
    }

    private TestGame createTestGame(double playerX, double playerY) {
        try {
            TileMap map = new TileMap("/maps/map.txt");
            Path highscoreFile = Files.createTempFile(
                    "jpac-panel-highscores",
                    ".json"
            );
            Files.deleteIfExists(highscoreFile);
            World world = new World(
                    map.getCols() * map.getTileSize(),
                    map.getRows() * map.getTileSize(),
                    map,
                    "Player",
                    new HighscoreManager(highscoreFile)
            );
            Player player = new Player(playerX, playerY);
            world.setPlayer(player);
            world.addEntity(player);

            GamePanel panel = new GamePanel(player, map, world);
            panel.setSize(800, 600);
            return new TestGame(panel, world, player);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
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

    private void moveMouse(GamePanel panel, int x, int y) {
        MouseEvent event = new MouseEvent(
                panel,
                MouseEvent.MOUSE_MOVED,
                System.currentTimeMillis(),
                0,
                x,
                y,
                0,
                false
        );

        for (MouseMotionListener listener : panel.getMouseMotionListeners()) {
            listener.mouseMoved(event);
        }
    }

    private void clickMouse(GamePanel panel, int x, int y) {
        MouseEvent event = new MouseEvent(
                panel,
                MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(),
                0,
                x,
                y,
                1,
                false
        );

        for (MouseListener listener : panel.getMouseListeners()) {
            listener.mouseClicked(event);
        }
    }

    private record TestGame(
            GamePanel panel,
            World world,
            Player player
    ) {
    }
}
