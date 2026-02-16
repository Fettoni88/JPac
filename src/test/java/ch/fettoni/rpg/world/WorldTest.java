package ch.fettoni.rpg.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {

    @Test
    void playerIsClampedAtLeftBorder() {

        World world = new World(800, 600);
        Player player = new Player(-50, 0);

        world.update(player, 0.0);

        assertEquals(0.0, player.getX(), 0.0001);
    }

    @Test
    void playerIsClampedAtRightBorder() {

        World world = new World(800, 600);
        Player player = new Player(1000, 0);

        world.update(player, 0.0);

        double expected = 800 - player.getSize();
        assertEquals(expected, player.getX(), 0.0001);
    }

    @Test
    void playerIsClampedAtBottomBorder() {

        World world = new World(800, 600);
        Player player = new Player(0, 1000);

        world.update(player, 0.0);

        double expected = 600 - player.getSize();
        assertEquals(expected, player.getY(), 0.0001);
    }

    @Test
    void worldAllowsMovementInsideBounds() {
        World world = new World(800, 600);
        Player player = new Player(100, 100);

        player.setDx(1);
        world.update(player, 1.0);

        assertTrue(player.getX() > 100);
    }

}
