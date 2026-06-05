package ffhs.jpac.world;

import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldTest {

    private World createWorld() {
        TileMap map = new TileMap("/maps/map.txt");
        return new World(
                map.getCols() * map.getTileSize(),
                map.getRows() * map.getTileSize(),
                map
        );
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
}
