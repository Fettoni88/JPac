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
    void playerRemainsCenteredOnCorridorLane() {
        World world = createWorld();
        Player player = new Player(27, 27);
        player.setDesiredDirection(Direction.RIGHT);

        for (int frame = 0; frame < 20; frame++) {
            player.update(world, 1.0 / 60.0);
        }

        assertTrue(player.getX() > 27);
        assertEquals(27, player.getY(), 0.0001);
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

    @Test
    void bufferedTurnIsUsedWhenIntersectionArrivesSoon() {
        World world = createWorld();
        Player player = new Player(27, 27);
        player.setDesiredDirection(Direction.RIGHT);
        player.update(world, 0.36);

        player.setDesiredDirection(Direction.DOWN);
        player.update(world, 0.12);
        player.update(world, 0.05);

        assertEquals(123, player.getX(), 0.0001);
        assertTrue(player.getY() > 27);
    }

    @Test
    void bufferedTurnExpiresBeforeLaterIntersection() {
        World world = createWorld();
        Player player = new Player(27, 27);
        player.setDesiredDirection(Direction.RIGHT);
        player.update(world, 0.12);

        player.setDesiredDirection(Direction.DOWN);
        player.update(world, 0.36);
        double yAtIntersection = player.getY();
        double xAtIntersection = player.getX();
        player.update(world, 0.05);

        assertTrue(player.getX() > xAtIntersection);
        assertEquals(yAtIntersection, player.getY(), 0.0001);
    }

    @Test
    void playerCannotMoveIntoWallTile() {
        TileMap map = new TileMap("/mazes/maze1.json");
        World world = createWorld(map);
        Player player = playerAt(map, 1, 1);
        player.setDesiredDirection(Direction.UP);

        player.update(world, 0.25);

        assertEquals(centeredTopLeft(map, 1), player.getX(), 0.0001);
        assertEquals(centeredTopLeft(map, 1), player.getY(), 0.0001);
    }

    @Test
    void playerCanMoveToWalkableFloorTile() {
        TileMap map = new TileMap("/mazes/maze1.json");
        World world = createWorld(map);
        Player player = playerAt(map, 1, 1);
        player.setDesiredDirection(Direction.RIGHT);

        player.update(world, 0.05);

        assertTrue(player.getX() > centeredTopLeft(map, 1));
        assertEquals(centeredTopLeft(map, 1), player.getY(), 0.0001);
    }

    @Test
    void playerCannotMoveFromExitIntoGhostHouse() {
        TileMap map = new TileMap("/mazes/maze1.json");
        World world = createWorld(map);
        int[] exit = map.findGhostHouseExit();
        Player player = playerAt(map, exit[2], exit[3]);
        Direction towardHouse = directionBetween(
                exit[2],
                exit[3],
                exit[0],
                exit[1]
        );
        player.setDesiredDirection(towardHouse);

        player.update(world, 0.25);

        assertEquals(
                centeredTopLeft(map, exit[3]),
                player.getX(),
                0.0001
        );
        assertEquals(
                centeredTopLeft(map, exit[2]),
                player.getY(),
                0.0001
        );
    }

    private World createWorld(TileMap map) {
        return new World(
                map.getCols() * map.getTileSize(),
                map.getRows() * map.getTileSize(),
                map
        );
    }

    private Player playerAt(TileMap map, int row, int col) {
        return new Player(
                map.getTileCenterX(col) - Player.SIZE / 2.0,
                map.getTileCenterY(row) - Player.SIZE / 2.0
        );
    }

    private double centeredTopLeft(TileMap map, int tileIndex) {
        return tileIndex * map.getTileSize()
                + (map.getTileSize() - Player.SIZE) / 2.0;
    }

    private Direction directionBetween(
            int fromRow,
            int fromCol,
            int toRow,
            int toCol
    ) {
        for (Direction direction : Direction.values()) {
            if (fromRow + direction.getDy() == toRow
                    && fromCol + direction.getDx() == toCol) {
                return direction;
            }
        }
        return Direction.NONE;
    }
}
