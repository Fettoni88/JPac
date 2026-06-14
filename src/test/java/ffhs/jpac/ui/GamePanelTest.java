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
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GamePanelTest {

    @Test
    void menuUsesFixedSize() {
        TestGame testGame = createTestGame(43, 43);

        assertEquals(
                new Dimension(800, 600),
                testGame.panel().getPreferredSize()
        );
    }

    @Test
    void selectedMazeSetsGameplaySize() {
        TestGame testGame = createTestGame(43, 43);

        openMazeOne(testGame.panel());

        TileMap map = testGame.world().getMap();
        assertEquals(
                new Dimension(
                        map.getCols() * map.getTileSize(),
                        map.getRows() * map.getTileSize() + 40
                ),
                testGame.panel().getPreferredSize()
        );
    }

    @Test
    void gameplayEntitiesUseScaledSizes() {
        assertEquals(18, new Player(0, 0).getSize());
        assertEquals(18, new Ghost(0, 0, Color.RED).getSize());
        assertEquals(5, new Pellet(0, 0).getSize());
    }

    @Test
    void gameplayRendersMazeBelowHudBar() {
        TestGame testGame = createTestGame(43, 43);
        Dimension size = testGame.panel().getPreferredSize();
        testGame.panel().setSize(size);
        testGame.world().startGame();
        BufferedImage image = new BufferedImage(
                size.width,
                size.height,
                BufferedImage.TYPE_INT_RGB
        );

        testGame.panel().paint(image.getGraphics());

        assertEquals(new Color(20, 20, 20).getRGB(), image.getRGB(2, 20));
        assertEquals(Color.DARK_GRAY.getRGB(), image.getRGB(2, 42));
    }

    @Test
    void startOptionOpensNameInputThenMazeSelection() {
        TestGame testGame = createTestGame(43, 43);

        pressKey(testGame.panel(), KeyEvent.VK_ENTER, '\n');
        assertEquals(GameState.NAME_INPUT, testGame.world().getGameState());

        pressKey(testGame.panel(), KeyEvent.VK_N, 'N');
        pressKey(testGame.panel(), KeyEvent.VK_E, 'e');
        pressKey(testGame.panel(), KeyEvent.VK_C, 'c');
        pressKey(testGame.panel(), KeyEvent.VK_I, 'i');
        pressKey(testGame.panel(), KeyEvent.VK_B, 'b');
        pressKey(testGame.panel(), KeyEvent.VK_ENTER, '\n');

        assertEquals(
                GameState.MAZE_SELECTION,
                testGame.world().getGameState()
        );
        assertEquals("Necib", testGame.world().getPlayerName());

        pressKey(testGame.panel(), KeyEvent.VK_ENTER, '\n');

        assertEquals(GameState.PLAYING, testGame.world().getGameState());
        assertEquals("maze1", testGame.world().getMap().getMazeId());
        assertEquals(5, testGame.world().getEntities().size());
    }

    @Test
    void emptyNameUsesPlayerAsDefault() {
        TestGame testGame = createTestGame(43, 43);

        pressKey(testGame.panel(), KeyEvent.VK_ENTER, '\n');
        pressKey(testGame.panel(), KeyEvent.VK_ENTER, '\n');

        assertEquals(
                GameState.MAZE_SELECTION,
                testGame.world().getGameState()
        );
        assertEquals("Player", testGame.world().getPlayerName());
    }

    @Test
    void mouseSelectsMazeAfterNameInput() {
        TestGame testGame = createTestGame(43, 43);

        pressKey(testGame.panel(), KeyEvent.VK_ENTER, '\n');
        pressKey(testGame.panel(), KeyEvent.VK_ENTER, '\n');
        moveMouse(testGame.panel(), 400, 314);
        clickMouse(testGame.panel(), 400, 314);

        assertEquals(GameState.PLAYING, testGame.world().getGameState());
        assertEquals("maze3", testGame.world().getMap().getMazeId());
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
    void endScreenRestartReloadsMazeAndKeepsHighscores() {
        TestGame testGame = createTestGame(43, 43);
        openMazeOne(testGame.panel());
        endGameWithGhostCollision(testGame.world());
        int highscoreCount = testGame.world().getHighscores().size();

        pressKey(testGame.panel(), KeyEvent.VK_ENTER, '\n');

        assertEquals(GameState.PLAYING, testGame.world().getGameState());
        assertEquals("maze1", testGame.world().getMap().getMazeId());
        assertEquals(0, testGame.world().getScore());
        assertEquals(highscoreCount, testGame.world().getHighscores().size());
    }

    @Test
    void endScreenMainMenuResetsSessionAndKeepsHighscores() {
        TestGame testGame = createTestGame(43, 43);
        openMazeOne(testGame.panel());
        endGameWithGhostCollision(testGame.world());
        int highscoreCount = testGame.world().getHighscores().size();

        pressKey(
                testGame.panel(),
                KeyEvent.VK_DOWN,
                KeyEvent.CHAR_UNDEFINED
        );
        pressKey(testGame.panel(), KeyEvent.VK_ENTER, '\n');

        assertEquals(GameState.START_MENU, testGame.world().getGameState());
        assertEquals(0, testGame.world().getScore());
        assertEquals(highscoreCount, testGame.world().getHighscores().size());
        assertEquals(
                new Dimension(800, 600),
                testGame.panel().getPreferredSize()
        );
    }

    @Test
    void endScreenExitRunsExitActionWithMouse() {
        AtomicBoolean exited = new AtomicBoolean();
        TestGame testGame = createTestGame(43, 43, () -> exited.set(true));
        openMazeOne(testGame.panel());
        endGameWithGhostCollision(testGame.world());
        testGame.panel().setSize(testGame.panel().getPreferredSize());

        moveMouse(testGame.panel(), 288, 692);
        clickMouse(testGame.panel(), 288, 692);

        assertTrue(exited.get());
    }

    @Test
    void arrowKeyMovesPlayerOnlyAfterNameIsConfirmed() {
        TestGame testGame = createTestGame(243, 315);

        pressKey(
                testGame.panel(),
                KeyEvent.VK_RIGHT,
                KeyEvent.CHAR_UNDEFINED
        );
        testGame.world().update(1.0);
        assertEquals(243, testGame.player().getX(), 0.0001);

        openMazeOne(testGame.panel());
        Player activePlayer = testGame.world().getPlayer();
        double startX = activePlayer.getX();
        double startY = activePlayer.getY();
        pressKey(
                testGame.panel(),
                KeyEvent.VK_RIGHT,
                KeyEvent.CHAR_UNDEFINED
        );

        for (int frame = 0; frame < 10; frame++) {
            testGame.world().update(1.0 / 60.0);
        }

        assertTrue(activePlayer.getX() > startX);
        assertEquals(startY, activePlayer.getY(), 0.0001);
    }

    private void openMazeOne(GamePanel panel) {
        pressKey(panel, KeyEvent.VK_ENTER, '\n');
        pressKey(panel, KeyEvent.VK_ENTER, '\n');
        pressKey(panel, KeyEvent.VK_ENTER, '\n');
    }

    private TestGame createTestGame(double playerX, double playerY) {
        return createTestGame(playerX, playerY, () -> {
            // Tests do not close the JVM.
        });
    }

    private TestGame createTestGame(
            double playerX,
            double playerY,
            Runnable exitAction
    ) {
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

            GamePanel panel = new GamePanel(world, exitAction);
            panel.setSize(800, 600);
            return new TestGame(panel, world, player);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void endGameWithGhostCollision(World world) {
        Ghost ghost = world.getEntities().stream()
                .filter(Ghost.class::isInstance)
                .map(Ghost.class::cast)
                .findFirst()
                .orElseThrow();
        ghost.setX(world.getPlayer().getX());
        ghost.setY(world.getPlayer().getY());
        world.update(0);
        assertEquals(GameState.GAME_OVER, world.getGameState());
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
