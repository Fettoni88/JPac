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
        Player player = new Player(27, 27);
        player.setDesiredDirection(Direction.RIGHT);
        world.setPlayer(player);
        world.addEntity(player);

        world.update(0.1);

        assertEquals(GameState.START_MENU, world.getGameState());
        assertEquals(27, player.getX(), 0.0001);
    }

    @Test
    void startGameAllowsPlayerMovement() {
        World world = createWorld();
        Player player = new Player(27, 27);
        player.setDesiredDirection(Direction.RIGHT);
        world.setPlayer(player);
        world.addEntity(player);
        world.startGame();

        world.update(0.1);

        assertTrue(player.getX() > 27);
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

        assertEquals(GameState.START_MENU, world.getGameState());
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
    void gameOverSavesPlayerHighscoreOnlyOnce() throws IOException {
        TileMap map = new TileMap("/maps/map.txt");
        Path file = Files.createTempFile("jpac-game-over-highscores", ".json");
        Files.deleteIfExists(file);
        HighscoreManager manager = new HighscoreManager(file);
        World world = new World(
                map.getCols() * map.getTileSize(),
                map.getRows() * map.getTileSize(),
                map,
                "Felipe",
                manager
        );
        Player player = new Player(43, 43);
        Ghost ghost = new Ghost(43, 43, Color.RED);
        world.setPlayer(player);
        world.addEntity(player);
        world.addEntity(ghost);
        world.addPellet(new Pellet(45, 45));
        world.addPellet(new Pellet(75, 43));
        world.startGame();

        world.update(0.0);
        world.update(0.0);

        List<HighscoreEntry> highscores = manager.loadHighscores();
        assertEquals(GameState.GAME_OVER, world.getGameState());
        assertEquals(1, highscores.size());
        assertEquals("Felipe", highscores.get(0).getName());
        assertEquals(10, highscores.get(0).getScore());
        assertEquals(1, world.getHighscores().size());
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
    void jsonMazeGeneratesOnlyDefinedPellets() {
        TileMap map = new TileMap("/mazes/maze1.json");
        World world = new World(
                map.getCols() * map.getTileSize(),
                map.getRows() * map.getTileSize(),
                map,
                "Player",
                createHighscoreManager()
        );

        world.generatePellets();

        assertEquals(
                map.getPelletPositions().size(),
                world.getPellets().size()
        );

        Pellet firstPellet = world.getPellets().getFirst();
        double pelletCenterX = firstPellet.getX()
                + firstPellet.getSize() / 2.0;
        double pelletCenterY = firstPellet.getY()
                + firstPellet.getSize() / 2.0;
        assertEquals(
                map.getTileSize() / 2.0,
                pelletCenterX % map.getTileSize(),
                0.0001
        );
        assertEquals(
                map.getTileSize() / 2.0,
                pelletCenterY % map.getTileSize(),
                0.0001
        );
    }

    @Test
    void playerTreatsGhostHouseAsBlockedButUnreleasedGhostDoesNot() {
        World world = createWorld();
        Player player = new Player(243, 267);
        Ghost ghost = new Ghost(
                243, 243, Color.RED, GhostPersonality.RED, 0
        );

        assertFalse(world.canMove(player, Direction.UP, 24));
        assertTrue(world.canMove(ghost, Direction.UP, 24));
        assertTrue(world.getMap().isGhostHouse(10, 10));
    }

    @Test
    void releasedGhostCanMoveInsideGhostHouse() {
        World world = createWorld();
        Player player = new Player(27, 27);
        Ghost ghost = new Ghost(
                243, 219, Color.RED, GhostPersonality.RED, 0
        );
        world.setPlayer(player);

        for (int frame = 0; frame < 120 && !ghost.hasLeftGhostHouse(); frame++) {
            ghost.update(world, 1.0 / 60.0);
        }

        assertTrue(ghost.hasLeftGhostHouse());

        ghost.setX(243);
        ghost.setY(267);

        assertTrue(world.canMove(ghost, Direction.UP, 24));
    }

    @Test
    void selectedMazeBuildsCompleteGameplaySession() {
        World world = createWorld();
        world.showNameInput();
        world.confirmPlayerName("Necib");

        world.startMaze("maze4");

        assertEquals(GameState.PLAYING, world.getGameState());
        assertEquals("maze4", world.getMap().getMazeId());
        assertEquals("Maze 4", world.getMap().getMazeName());
        assertEquals(5, world.getEntities().size());
        assertEquals(4, world.getEntities().stream()
                .filter(Ghost.class::isInstance)
                .count());
        assertEquals(
                world.getMap().getPelletPositions().size(),
                world.getPellets().size()
        );
    }

    @Test
    void selectedMazeIsStoredWithHighscore() throws IOException {
        TileMap initialMap = new TileMap("/mazes/maze1.json");
        Path file = Files.createTempFile(
                "jpac-selected-maze-highscores",
                ".json"
        );
        Files.deleteIfExists(file);
        HighscoreManager manager = new HighscoreManager(file);
        World world = new World(
                initialMap.getCols() * initialMap.getTileSize(),
                initialMap.getRows() * initialMap.getTileSize(),
                initialMap,
                "Player",
                manager
        );
        world.showNameInput();
        world.confirmPlayerName("Felipe");
        world.startMaze("maze5");

        Ghost ghost = world.getEntities().stream()
                .filter(Ghost.class::isInstance)
                .map(Ghost.class::cast)
                .findFirst()
                .orElseThrow();
        ghost.setX(world.getPlayer().getX());
        ghost.setY(world.getPlayer().getY());
        world.update(0);

        HighscoreEntry entry = manager.loadHighscores().getFirst();
        assertEquals("maze5", entry.getMazeId());
        assertEquals("Maze 5", entry.getMazeName());
    }
}
