package ffhs.jpac.world;

import ffhs.jpac.persistence.HighscoreEntry;
import ffhs.jpac.persistence.HighscoreManager;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldTest {

    private World createWorld() {
        TileMap map = new TileMap("/maps/map.txt");
        return new World(
                map.getCols() * map.getTileSize(),
                map.getRows() * map.getTileSize(),
                map,
                "Player",
                createHighscoreManager()
        );
    }

    private HighscoreManager createHighscoreManager() {
        try {
            Path file = Files.createTempFile("jpac-world-highscores", ".json");
            Files.deleteIfExists(file);
            return new HighscoreManager(file);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @Test
    void worldWaitsForStart() {
        World world = createWorld();
        Player player = new Player(43, 43);
        player.setDesiredDirection(Direction.RIGHT);
        world.setPlayer(player);
        world.addEntity(player);

        world.update(0.1);

        assertEquals(GameState.START, world.getGameState());
        assertEquals(43, player.getX(), 0.0001);
    }

    @Test
    void startGameAllowsPlayerMovement() {
        World world = createWorld();
        Player player = new Player(43, 43);
        player.setDesiredDirection(Direction.RIGHT);
        world.setPlayer(player);
        world.addEntity(player);
        world.startGame();

        world.update(0.1);

        assertTrue(player.getX() > 43);
    }

    @Test
    void ghostCollisionEndsGame() {
        World world = createWorld();
        Player player = new Player(43, 43);
        Ghost ghost = new Ghost(43, 43, Color.RED);
        world.setPlayer(player);
        world.addEntity(player);
        world.addEntity(ghost);
        world.addPellet(new Pellet(75, 43));
        world.startGame();

        world.update(0.0);

        assertEquals(GameState.GAME_OVER, world.getGameState());
        assertTrue(world.isGameOver());
    }

    @Test
    void runningGameIsNotGameOver() {
        World world = createWorld();
        world.addPellet(new Pellet(43, 43));
        world.startGame();

        assertFalse(world.isGameOver());
    }

    @Test
    void restartResetsGameAndEntityPositions() {
        World world = createWorld();
        Player player = new Player(43, 43);
        Ghost ghost = new Ghost(75, 43, Color.CYAN);
        world.setPlayer(player);
        world.addEntity(player);
        world.addEntity(ghost);
        world.addPellet(new Pellet(45, 45));
        world.startGame();
        world.update(0.0);

        player.setX(100);
        player.setY(100);
        ghost.setX(120);
        ghost.setY(120);

        world.restartGame();

        assertEquals(GameState.START, world.getGameState());
        assertEquals(0, world.getScore());
        assertTrue(world.getPellets().size() > 1);
        assertTrue(world.getPellets().stream().noneMatch(Pellet::isCollected));
        assertEquals(43, player.getX(), 0.0001);
        assertEquals(43, player.getY(), 0.0001);
        assertEquals(75, ghost.getX(), 0.0001);
        assertEquals(43, ghost.getY(), 0.0001);
        assertEquals(Color.CYAN, ghost.getColor());
    }

    @Test
    void collectingLastPelletWinsGame() {
        World world = createWorld();
        Player player = new Player(43, 43);
        world.setPlayer(player);
        world.addEntity(player);
        world.addPellet(new Pellet(45, 45));
        world.startGame();

        world.update(0.0);

        assertEquals(GameState.WIN, world.getGameState());
        assertEquals(10, world.getScore());
    }

    @Test
    void winningSavesPlayerHighscoreOnlyOnce() throws IOException {
        TileMap map = new TileMap("/maps/map.txt");
        Path file = Files.createTempFile("jpac-win-highscores", ".json");
        Files.deleteIfExists(file);
        HighscoreManager manager = new HighscoreManager(file);
        World world = new World(
                map.getCols() * map.getTileSize(),
                map.getRows() * map.getTileSize(),
                map,
                "  Necib  ",
                manager
        );
        Player player = new Player(43, 43);
        world.setPlayer(player);
        world.addEntity(player);
        world.addPellet(new Pellet(45, 45));
        world.startGame();

        world.update(0.0);
        world.update(0.0);

        List<HighscoreEntry> highscores = manager.loadHighscores();
        assertEquals("Necib", world.getPlayerName());
        assertEquals(1, highscores.size());
        assertEquals("Necib", highscores.get(0).getName());
        assertEquals(10, highscores.get(0).getScore());
    }

    @Test
    void pelletsOnlyUseReachableNormalFloorTiles() {
        World world = createWorld();
        Player player = new Player(331, 427);
        world.setPlayer(player);

        world.generatePellets();

        int floorTileCount = 0;
        for (int row = 0; row < world.getMap().getRows(); row++) {
            for (int col = 0; col < world.getMap().getCols(); col++) {
                if (world.getMap().isPelletTile(row, col)) {
                    floorTileCount++;
                }
            }
        }

        assertEquals(floorTileCount, world.getPellets().size());
        assertTrue(world.getPellets().stream().noneMatch(pellet -> {
            int col = (int) (pellet.getX() / world.getMap().getTileSize());
            int row = (int) (pellet.getY() / world.getMap().getTileSize());
            return world.getMap().getTile(row, col) == TileType.GHOST_HOUSE;
        }));
    }

    @Test
    void playerTreatsGhostHouseAsBlockedButUnreleasedGhostDoesNot() {
        World world = createWorld();
        Player player = new Player(331, 363);
        Ghost ghost = new Ghost(
                331, 331, Color.RED, GhostPersonality.RED, 0
        );

        assertFalse(world.canMove(player, Direction.UP, 32));
        assertTrue(world.canMove(ghost, Direction.UP, 32));
        assertTrue(world.getMap().isGhostHouse(10, 10));
    }

    @Test
    void releasedGhostTreatsGhostHouseAsBlocked() {
        World world = createWorld();
        Player player = new Player(43, 43);
        Ghost ghost = new Ghost(
                331, 299, Color.RED, GhostPersonality.RED, 0
        );
        world.setPlayer(player);

        for (int frame = 0; frame < 120 && !ghost.hasLeftGhostHouse(); frame++) {
            ghost.update(world, 1.0 / 60.0);
        }

        assertTrue(ghost.hasLeftGhostHouse());

        ghost.setX(331);
        ghost.setY(363);

        assertFalse(world.canMove(ghost, Direction.UP, 32));
    }
}
