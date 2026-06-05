package ffhs.jpac.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerTest {

    private World createWorld() {
        TileMap map = new TileMap("/maps/map.txt");
        return new World(
                map.getCols() * map.getTileSize(),
                map.getRows() * map.getTileSize(),
                map
        );
    }

    @Test
    void playerDoesNotMoveWhenNoDirectionIsSet() {
        World world = createWorld();
        Player player = new Player(43, 43);

        player.update(world, 1.0);

        assertEquals(43, player.getX(), 0.0001);
        assertEquals(43, player.getY(), 0.0001);
    }

    @Test
    void playerMovesRightWithDeltaTime() {
        World world = createWorld();
        Player player = new Player(43, 43);
        player.setDesiredDirection(Direction.RIGHT);

        player.update(world, 0.05);

        assertTrue(player.getX() > 43);
        assertEquals(43, player.getY(), 0.0001);
    }

    @Test
    void deltaTimeZeroDoesNotMovePlayer() {
        World world = createWorld();
        Player player = new Player(43, 43);
        player.setDesiredDirection(Direction.RIGHT);

        player.update(world, 0.0);

        assertEquals(43, player.getX(), 0.0001);
    }

    @Test
    void playerWaitsForTileCenterBeforeTurning() {
        World world = createWorld();
        Player player = new Player(43, 43);
        player.setDesiredDirection(Direction.RIGHT);
        player.update(world, 0.05);

        player.setDesiredDirection(Direction.DOWN);
        player.update(world, 0.01);

        assertTrue(player.getX() > 43);
        assertEquals(43, player.getY(), 0.0001);
    }

    @Test
    void playerCorrectsTowardLaneCenterSmoothly() {
        World world = createWorld();
        Player player = new Player(43, 115);
        player.setDesiredDirection(Direction.RIGHT);

        player.update(world, 1.0 / 60.0);

        assertTrue(player.getX() > 43);
        assertTrue(player.getY() < 115);
        assertTrue(player.getY() > 107);
    }

    @Test
    void playerKeepsMovingAcrossMultipleFrames() {
        World world = createWorld();
        Player player = new Player(331, 427);
        player.setDesiredDirection(Direction.RIGHT);

        for (int frame = 0; frame < 10; frame++) {
            player.update(world, 1.0 / 60.0);
        }

        assertTrue(player.getX() > 350);
        assertEquals(427, player.getY(), 0.0001);
    }

    @Test
    void playerCannotEnterGhostHouse() {
        World world = createWorld();
        Player player = new Player(331, 363);
        player.setDesiredDirection(Direction.UP);

        for (int frame = 0; frame < 30; frame++) {
            player.update(world, 1.0 / 60.0);
        }

        int tileSize = world.getMap().getTileSize();
        int row = (int) ((player.getY() + player.getSize() / 2.0) / tileSize);
        int col = (int) ((player.getX() + player.getSize() / 2.0) / tileSize);

        assertTrue(player.getY() >= 352);
        assertTrue(!world.getMap().isGhostHouse(row, col));
    }

    @Test
    void bufferedTurnWorksWhenCurrentDirectionIsBlocked() {
        World world = createWorld();
        Player player = new Player(267, 75);
        player.setDesiredDirection(Direction.UP);

        for (int frame = 0; frame < 20; frame++) {
            player.update(world, 1.0 / 60.0);
        }

        double blockedY = player.getY();

        player.setDesiredDirection(Direction.LEFT);
        player.update(world, 1.0 / 60.0);

        assertTrue(player.getX() < 267);
        assertEquals(blockedY, player.getY(), 2);
    }
}
