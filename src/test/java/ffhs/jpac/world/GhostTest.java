package ffhs.jpac.world;

import ffhs.jpac.maze.MazePosition;
import org.junit.jupiter.api.Test;

import java.awt.Color;

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
                243, 195, Color.PINK, GhostPersonality.PINK, 3
        );

        pink.update(world, 2.9);
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
                267, 195, Color.CYAN, GhostPersonality.CYAN, 6
        );

        cyan.update(world, 6);
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
                243, 219, Color.ORANGE, GhostPersonality.ORANGE, 9
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
                new Ghost(243, 195, Color.PINK, GhostPersonality.PINK, 3),
                new Ghost(267, 195, Color.CYAN, GhostPersonality.CYAN, 6),
                new Ghost(243, 219, Color.ORANGE, GhostPersonality.ORANGE, 9)
        };

        for (int second = 0; second < 11; second++) {
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
            MazePosition ghostSpawn = map.getGhostSpawns().getFirst();
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
                    "Ghost did not leave maze " + mazeNumber
            );
        }
    }
}
