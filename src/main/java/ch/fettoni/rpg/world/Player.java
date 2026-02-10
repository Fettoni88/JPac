package ch.fettoni.rpg.world;

public class Player {

    private int x;
    private int y;

    private static final int SIZE = 10;
    private static final int SPEED = 2;

    private static final int WORLD_WIDTH = 800;
    private static final int WORLD_HEIGHT = 600;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void moveRight() {
        x += SPEED;
    }

    public int getX() {
        return x;
    }

    public void clampX(int min, int max) {
        if (x < min) {
            x = min;
        }
        if (x > max) {
            x = max;
        }
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return SIZE;
    }
}
