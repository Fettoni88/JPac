package ch.fettoni.rpg.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void playerDoesNotMoveWhenNoDirectionSet() {

        Player player = new Player(0, 0);

        player.update(1.0);

        assertEquals(0.0, player.getX(), 0.0001);
        assertEquals(0.0, player.getY(), 0.0001);
    }

    @Test
    void playerMovesRightWithDeltaTime() {

        Player player = new Player(0, 0);
        player.setDx(1);
        player.setDy(0);

        player.update(1.0); // 1 Sekunde

        assertEquals(200.0, player.getX(), 0.0001);
        assertEquals(0.0, player.getY(), 0.0001);
    }

    @Test
    void playerMovesLeftCorrectly() {

        Player player = new Player(0, 0);
        player.setDx(-1);

        player.update(0.5); // halbe Sekunde

        assertEquals(-100.0, player.getX(), 0.0001);
    }

    @Test
    void diagonalMovementIsNormalized() {

        Player player = new Player(0, 0);
        player.setDx(1);
        player.setDy(1);

        player.update(1.0);

        double expected = 200.0 / Math.sqrt(2);

        assertEquals(expected, player.getX(), 0.0001);
        assertEquals(expected, player.getY(), 0.0001);
    }

    @Test
    void clampXStopsAtMinimum() {
        Player player = new Player(-50, 0);
        player.clampX(0, 800);
        assertEquals(0.0, player.getX(), 0.0001);
    }

    @Test
    void clampXStopsAtMaximum() {
        Player player = new Player(900, 0);
        player.clampX(0, 800);
        assertEquals(800.0, player.getX(), 0.0001);
    }

    @Test
    void clampYStopsAtMinimum() {
        Player player = new Player(0, -100);
        player.clampY(0, 600);
        assertEquals(0.0, player.getY(), 0.0001);
    }

    @Test
    void clampYStopsAtMaximum() {
        Player player = new Player(0, 700);
        player.clampY(0, 600);
        assertEquals(600.0, player.getY(), 0.0001);
    }

    @Test
    void deltaTimeZeroDoesNotMovePlayer() {
        Player player = new Player(0, 0);
        player.setDx(1);
        player.update(0.0);
        assertEquals(0.0, player.getX(), 0.0001);
    }

}

