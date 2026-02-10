package ch.fettoni.rpg.world;

public class World {

    private final int width;
    private final int height;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void update(Player player) {
        player.moveRight();
        player.clampX(0, width - player.getSize());
    }
}
