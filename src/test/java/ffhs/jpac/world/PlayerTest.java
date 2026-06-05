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
    void playerCanTurnWithoutExactTileCenter() {
        World world = createWorld();
        Player player = new Player(43, 107);
        player.setDesiredDirection(Direction.RIGHT);
        player.update(world, 0.03);

        player.setDesiredDirection(Direction.DOWN);
        player.update(world, 0.03);

        assertTrue(player.getX() > 43);
        assertTrue(player.getY() > 107);
    }

    @Test
    void playerKeepsOffsetInsideOpenCorridor() {
        World world = createWorld();
        Player player = new Player(43, 110);
        player.setDesiredDirection(Direction.RIGHT);

        player.update(world, 0.05);

        assertTrue(player.getX() > 43);
        assertEquals(110, player.getY(), 0.0001);
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
}
