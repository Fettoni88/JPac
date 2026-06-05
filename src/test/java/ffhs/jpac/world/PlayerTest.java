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
}
