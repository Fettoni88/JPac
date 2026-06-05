package ffhs.jpac.world;

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
        Player player = new Player(331, 427);
        World world = createWorld(player);
        Ghost pink = new Ghost(
                331, 267, Color.PINK, GhostPersonality.PINK, 3
        );

        pink.update(world, 2.9);
        pink.update(world, 0.1);

        assertTrue(pink.isReleased());
        assertEquals(331, pink.getX(), 0.0001);
        assertEquals(267, pink.getY(), 0.0001);

        pink.update(world, 0.1);

        assertNotEquals(267, pink.getY(), 0.0001);
    }

    @Test
    void restartResetsReleaseDelay() {
        Player player = new Player(331, 427);
        World world = createWorld(player);
        Ghost cyan = new Ghost(
                363, 267, Color.CYAN, GhostPersonality.CYAN, 6
        );

        cyan.update(world, 6);
        assertTrue(cyan.isReleased());

        cyan.reset();

        assertFalse(cyan.isReleased());
        assertEquals(363, cyan.getX(), 0.0001);
        assertEquals(267, cyan.getY(), 0.0001);
    }

    @Test
    void ghostMovementStaysOnOneGridLane() {
        Player player = new Player(331, 427);
        World world = createWorld(player);
        Ghost red = new Ghost(
                299, 267, Color.RED, GhostPersonality.RED, 0
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
        Player player = new Player(331, 427);
        World world = createWorld(player);
        Ghost red = new Ghost(
                299, 267, Color.RED, GhostPersonality.RED, 0
        );

        for (int frame = 0; frame < 120; frame++) {
            red.update(world, 1.0 / 60.0);
        }

        double distanceFromSpawn = Math.hypot(
                red.getX() - 299,
                red.getY() - 267
        );
        assertTrue(distanceFromSpawn > 20);
    }

    @Test
    void redGhostLeavesGhostHouseInsteadOfPushingIntoWall() {
        Player player = new Player(331, 427);
        World world = createWorld(player);
        Ghost red = new Ghost(
                299, 267, Color.RED, GhostPersonality.RED, 0
        );

        for (int frame = 0; frame < 120; frame++) {
            red.update(world, 1.0 / 60.0);
        }

        assertTrue(red.hasLeftGhostHouse());
        assertTrue(red.getY() >= 352);
    }

    @Test
    void ghostsKeepTheirConfiguredPersonalities() {
        Ghost orange = new Ghost(
                331, 299, Color.ORANGE, GhostPersonality.ORANGE, 9
        );

        assertEquals(GhostPersonality.ORANGE, orange.getPersonality());
        assertEquals(Color.ORANGE, orange.getColor());
    }

    @Test
    void allGhostsUseTheirDelayThenLeaveThroughExit() {
        Player player = new Player(43, 43);
        World world = createWorld(player);
        Ghost[] ghosts = {
                new Ghost(299, 267, Color.RED, GhostPersonality.RED, 0),
                new Ghost(331, 267, Color.PINK, GhostPersonality.PINK, 3),
                new Ghost(363, 267, Color.CYAN, GhostPersonality.CYAN, 6),
                new Ghost(331, 299, Color.ORANGE, GhostPersonality.ORANGE, 9)
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
        Player player = new Player(43, 43);
        World world = createWorld(player);
        Ghost red = new Ghost(
                299, 267, Color.RED, GhostPersonality.RED, 0
        );

        for (int frame = 0; frame < 20; frame++) {
            red.update(world, 1.0 / 60.0);
        }

        assertEquals(331, red.getX(), 0.0001);
        assertTrue(red.getY() > 267);
        assertTrue(red.getY() < 331);
        assertFalse(red.hasLeftGhostHouse());
    }
}
