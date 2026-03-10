package ch.fettoni.rpg.world;

public class World {

    private final int width;
    private final int height;
    private final TileMap map;

    public World(int width, int height, TileMap map) {
        this.width = width;
        this.height = height;
        this.map = map;
    }

    public void update(Player player, double deltaTime) {

        double oldX = player.getX();
        double oldY = player.getY();

        player.moveX(deltaTime);
        if (isColliding(player)) {
            player.setX(Math.round(oldX));
        }

        player.moveY(deltaTime);
        if (isColliding(player)) {
            player.setY(Math.round(oldY));
        }

        player.clampX(0, width - player.getSize());
        player.clampY(0, height - player.getSize());
    }

    private boolean isColliding(Player player) {

        int tileSize = map.getTileSize();

        int leftTile   = (int) (player.getX() / tileSize);
        int rightTile  = (int) ((player.getX() + player.getSize() - 1) / tileSize);
        int topTile    = (int) (player.getY() / tileSize);
        int bottomTile = (int) ((player.getY() + player.getSize() - 1) / tileSize);

        return map.isWall(topTile, leftTile) ||
                map.isWall(topTile, rightTile) ||
                map.isWall(bottomTile, leftTile) ||
                map.isWall(bottomTile, rightTile);
    }
}//end of class
