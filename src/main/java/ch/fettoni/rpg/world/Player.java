package ch.fettoni.rpg.world;

public class Player {

    private double x;
    private double y;

    private int dx = 0;
    private int dy = 0;

    private static final int SIZE = 10;
    private static final double SPEED = 200.0; // Pixel pro Sekunde

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }

    public void update(double deltaTime) {
        double moveX = dx;
        double moveY = dy;

        if (dx != 0 && dy != 0) {
            double factor = 1 / Math.sqrt(2);
            moveX *= factor;
            moveY *= factor;
        }

        x += moveX * SPEED * deltaTime;
        y += moveY * SPEED * deltaTime;
    }

    public void clampX(double min, double max) {
        if (x < min) x = min;
        if (x > max) x = max;
    }

    public void clampY(double min, double max) {
        if (y < min) y = min;
        if (y > max) y = max;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getSize() {
        return SIZE;
    }
}