package ffhs.jpac.world;

import ffhs.jpac.maze.MazePosition;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GhostTest {

    private World createWorld(Player player) {
        TileMap map = new TileMap("/maps/map.txt");
        World world = new World(
                map.getCols() * map.getTileSize(),
                map.getRows() * map.getTileSize(),
                map
        );
        world.setPlayer(player);
        return world;
    }

    @Test
    void delayedGhostWaitsBeforeMoving() {
        Player player = new Player(243, 315);
        World world = createWorld(player);
        Ghost pink = new Ghost(
                243, 195, Color.PINK, GhostPersonality.PINK, 5
        );

        pink.update(world, 4.9);
        pink.update(world, 0.1);

        assertTrue(pink.isReleased());
        assertEquals(243, pink.getX(), 0.0001);
        assertEquals(195, pink.getY(), 0.0001);

        pink.update(world, 0.1);

        assertNotEquals(195, pink.getY(), 0.0001);
    }

    @Test
    void restartResetsReleaseDelay() {
        Player player = new Player(243, 315);
        World world = createWorld(player);
        Ghost cyan = new Ghost(
                267, 195, Color.CYAN, GhostPersonality.CYAN, 10
        );

        cyan.update(world, 10);
        assertTrue(cyan.isReleased());

        cyan.reset();

        assertFalse(cyan.isReleased());
        assertEquals(267, cyan.getX(), 0.0001);
        assertEquals(195, cyan.getY(), 0.0001);
    }

    @Test
    void ghostMovementStaysOnOneGridLane() {
        Player player = new Player(243, 315);
        World world = createWorld(player);
        Ghost red = new Ghost(
                219, 195, Color.RED, GhostPersonality.RED, 0
        );

        double oldX = red.getX();
        double oldY = red.getY();
        red.update(world, 0.05);

        boolean movedHorizontally = red.getX() != oldX && red.getY() == oldY;
        boolean movedVertically = red.getY() != oldY && red.getX() == oldX;
        assertTrue(movedHorizontally || movedVertically);
    }

    @Test
    void releasedGhostKeepsMovingBeyondSpawnTile() {
        Player player = new Player(243, 315);
        World world = createWorld(player);
        Ghost red = new Ghost(
                219, 195, Color.RED, GhostPersonality.RED, 0
        );

        for (int frame = 0; frame < 120; frame++) {
            red.update(world, 1.0 / 60.0);
        }

        double distanceFromSpawn = Math.hypot(
                red.getX() - 219,
                red.getY() - 195
        );
        assertTrue(distanceFromSpawn > 20);
    }

    @Test
    void redGhostLeavesGhostHouseInsteadOfPushingIntoWall() {
        Player player = new Player(243, 315);
        World world = createWorld(player);
        Ghost red = new Ghost(
                219, 195, Color.RED, GhostPersonality.RED, 0
        );

        for (int frame = 0; frame < 120; frame++) {
            red.update(world, 1.0 / 60.0);
        }

        assertTrue(red.hasLeftGhostHouse());
        assertTrue(red.getY() >= 264);
    }

    @Test
    void ghostsKeepTheirConfiguredPersonalities() {
        Ghost orange = new Ghost(
                243, 219, Color.ORANGE, GhostPersonality.ORANGE, 15
        );

        assertEquals(GhostPersonality.ORANGE, orange.getPersonality());
        assertEquals(Color.ORANGE, orange.getColor());
    }

    @Test
    void allGhostsUseTheirDelayThenLeaveThroughExit() {
        Player player = new Player(27, 27);
        World world = createWorld(player);
        Ghost[] ghosts = {
                new Ghost(219, 195, Color.RED, GhostPersonality.RED, 0),
                new Ghost(243, 195, Color.PINK, GhostPersonality.PINK, 5),
                new Ghost(267, 195, Color.CYAN, GhostPersonality.CYAN, 10),
                new Ghost(243, 219, Color.ORANGE, GhostPersonality.ORANGE, 15)
        };

        for (int second = 0; second < 18; second++) {
            for (int frame = 0; frame < 60; frame++) {
                for (Ghost ghost : ghosts) {
                    ghost.update(world, 1.0 / 60.0);
                }
            }
        }

        for (Ghost ghost : ghosts) {
            assertTrue(ghost.isReleased());
            assertTrue(ghost.hasLeftGhostHouse());
        }
    }

    @Test
    void firstGhostReleasesImmediatelyAndLaterGhostUsesDelay() {
        World world = createWorld(new Player(27, 27));
        Ghost red = new Ghost(
                219, 195, Color.RED, GhostPersonality.RED, 0
        );
        Ghost pink = new Ghost(
                243, 195, Color.PINK, GhostPersonality.PINK, 5
        );

        assertTrue(red.isReleased());
        assertFalse(pink.isReleased());

        pink.update(world, 4.9);
        assertFalse(pink.isReleased());

        pink.update(world, 0.1);
        assertTrue(pink.isReleased());
    }

    @Test
    void idleGhostDoesNotReverseWhenAnotherDirectionIsAvailable() {
        TileMap map = new TileMap("/mazes/maze1.json");
        World world = createWorldWithMap(map);
        Ghost ghost = findGhostAtJunction(world, Direction.RIGHT);

        ghost.setDirection(Direction.RIGHT);
        assertTrue(ghost.chooseIdleDirection(world));

        assertNotEquals(Direction.LEFT, ghost.getDirection());
    }

    @Test
    void idleGhostReversesAtDeadEnd() {
        TileMap map = new TileMap("/mazes/maze1.json");
        World world = createWorldWithMap(map);
        Ghost ghost = findGhostAtDeadEnd(world);
        Direction onlyExit = findAvailableDirections(world, ghost).getFirst();

        ghost.setDirection(onlyExit.opposite());
        assertTrue(ghost.chooseIdleDirection(world));

        assertEquals(onlyExit, ghost.getDirection());
    }

    @Test
    void ghostReleaseUsesCenterColumnBeforeDoorway() {
        Player player = new Player(27, 27);
        World world = createWorld(player);
        Ghost red = new Ghost(
                219, 195, Color.RED, GhostPersonality.RED, 0
        );

        for (int frame = 0; frame < 20; frame++) {
            red.update(world, 1.0 / 60.0);
        }

        assertEquals(243, red.getX(), 0.0001);
        assertTrue(red.getY() > 195);
        assertTrue(red.getY() < 243);
        assertFalse(red.hasLeftGhostHouse());
    }

    @Test
    void ghostsLeaveTheHouseInAllJsonMazes() {
        for (int mazeNumber = 1; mazeNumber <= 5; mazeNumber++) {
            TileMap map = new TileMap(
                    "/mazes/maze" + mazeNumber + ".json"
            );
            MazePosition playerSpawn = map.getPlayerSpawn();
            int tileSize = map.getTileSize();
            Player player = new Player(
                    playerSpawn.col() * tileSize
                            + (tileSize - Player.SIZE) / 2.0,
                    playerSpawn.row() * tileSize
                            + (tileSize - Player.SIZE) / 2.0
            );
            World world = new World(
                    map.getCols() * tileSize,
                    map.getRows() * tileSize,
                    map
            );
            world.setPlayer(player);
            for (MazePosition ghostSpawn : map.getGhostSpawns()) {
                Ghost red = new Ghost(
                        ghostSpawn.col() * tileSize
                                + (tileSize - Ghost.SIZE) / 2.0,
                        ghostSpawn.row() * tileSize
                                + (tileSize - Ghost.SIZE) / 2.0,
                        Color.RED,
                        GhostPersonality.RED,
                        0
                );

                for (int frame = 0;
                     frame < 180 && !red.hasLeftGhostHouse();
                     frame++) {
                    red.update(world, 1.0 / 60.0);
                }

                assertTrue(red.isReleased());
                assertTrue(
                        red.hasLeftGhostHouse(),
                        "Ghost at " + ghostSpawn
                                + " did not leave maze " + mazeNumber
                );
            }
        }
    }

    private World createWorldWithMap(TileMap map) {
        return new World(
                map.getCols() * map.getTileSize(),
                map.getRows() * map.getTileSize(),
                map
        );
    }

    private Ghost findGhostAtJunction(
            World world,
            Direction currentDirection
    ) {
        for (int row = 1; row < world.getMap().getRows() - 1; row++) {
            for (int col = 1; col < world.getMap().getCols() - 1; col++) {
                Ghost ghost = createCenteredGhost(world, row, col);
                List<Direction> directions =
                        findAvailableDirections(world, ghost);

                if (directions.contains(currentDirection)
                        && directions.size() >= 3) {
                    return ghost;
                }
            }
        }

        throw new IllegalStateException("Maze has no suitable junction");
    }

    private Ghost findGhostAtDeadEnd(World world) {
        for (int row = 1; row < world.getMap().getRows() - 1; row++) {
            for (int col = 1; col < world.getMap().getCols() - 1; col++) {
                Ghost ghost = createCenteredGhost(world, row, col);
                if (findAvailableDirections(world, ghost).size() == 1) {
                    return ghost;
                }
            }
        }

        throw new IllegalStateException("Maze has no dead end");
    }

    private Ghost createCenteredGhost(World world, int row, int col) {
        int tileSize = world.getMap().getTileSize();
        return new Ghost(
                col * tileSize + (tileSize - Ghost.SIZE) / 2.0,
                row * tileSize + (tileSize - Ghost.SIZE) / 2.0,
                Color.CYAN,
                GhostPersonality.CYAN,
                0
        );
    }

    private List<Direction> findAvailableDirections(
            World world,
            Ghost ghost
    ) {
        List<Direction> directions = new ArrayList<>();

        for (Direction direction : List.of(
                Direction.UP,
                Direction.DOWN,
                Direction.LEFT,
                Direction.RIGHT
        )) {
            if (world.canMove(
                    ghost,
                    direction,
                    world.getMap().getTileSize()
            )) {
                directions.add(direction);
            }
        }

        return directions;
    }
}
