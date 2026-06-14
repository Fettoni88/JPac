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
        Player player = new Player(27, 27);

        player.update(world, 1.0);

        assertEquals(27, player.getX(), 0.0001);
        assertEquals(27, player.getY(), 0.0001);
    }

    @Test
    void playerMovesRightWithDeltaTime() {
        World world = createWorld();
        Player player = new Player(27, 27);
        player.setDesiredDirection(Direction.RIGHT);

        player.update(world, 0.05);

        assertTrue(player.getX() > 27);
        assertEquals(27, player.getY(), 0.0001);
    }

    @Test
    void deltaTimeZeroDoesNotMovePlayer() {
        World world = createWorld();
        Player player = new Player(27, 27);
        player.setDesiredDirection(Direction.RIGHT);

        player.update(world, 0.0);

        assertEquals(27, player.getX(), 0.0001);
    }

    @Test
    void playerWaitsForTileCenterBeforeTurning() {
        World world = createWorld();
        Player player = new Player(27, 27);
        player.setDesiredDirection(Direction.RIGHT);
        player.update(world, 0.05);

        player.setDesiredDirection(Direction.DOWN);
        player.update(world, 0.01);

        assertTrue(player.getX() > 27);
        assertEquals(27, player.getY(), 0.0001);
    }

    @Test
    void playerCorrectsTowardLaneCenterSmoothly() {
        World world = createWorld();
        Player player = new Player(27, 83);
        player.setDesiredDirection(Direction.RIGHT);

        player.update(world, 1.0 / 60.0);

        assertTrue(player.getX() > 27);
        assertTrue(player.getY() < 83);
        assertTrue(player.getY() > 75);
    }

    @Test
    void playerKeepsMovingAcrossMultipleFrames() {
        World world = createWorld();
        Player player = new Player(243, 315);
        player.setDesiredDirection(Direction.RIGHT);

        for (int frame = 0; frame < 10; frame++) {
            player.update(world, 1.0 / 60.0);
        }

        assertTrue(player.getX() > 262);
        assertEquals(315, player.getY(), 0.0001);
    }

    @Test
    void playerCannotEnterGhostHouse() {
        World world = createWorld();
        Player player = new Player(243, 267);
        player.setDesiredDirection(Direction.UP);

        for (int frame = 0; frame < 30; frame++) {
            player.update(world, 1.0 / 60.0);
        }

        int tileSize = world.getMap().getTileSize();
        int row = (int) ((player.getY() + player.getSize() / 2.0) / tileSize);
        int col = (int) ((player.getX() + player.getSize() / 2.0) / tileSize);

        assertTrue(player.getY() >= 264);
        assertTrue(!world.getMap().isGhostHouse(row, col));
    }

    @Test
    void bufferedTurnWorksWhenCurrentDirectionIsBlocked() {
        World world = createWorld();
        Player player = new Player(195, 51);
        player.setDesiredDirection(Direction.UP);

        for (int frame = 0; frame < 20; frame++) {
            player.update(world, 1.0 / 60.0);
        }

        double blockedY = player.getY();

        player.setDesiredDirection(Direction.LEFT);
        player.update(world, 1.0 / 60.0);

        assertTrue(player.getX() < 195);
        assertEquals(blockedY, player.getY(), 2);
    }
}
