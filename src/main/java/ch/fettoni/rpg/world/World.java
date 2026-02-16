package ch.fettoni.rpg.world;

public class World {

    private final int width;
    private final int height;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void update(Player player, double deltaTime) {
        player.update(deltaTime);
        player.clampX(0, width - player.getSize());
        player.clampY(0, height - player.getSize());
    }
}//end of class
