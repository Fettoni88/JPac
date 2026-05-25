package ffhs.jpac.world;

import java.util.ArrayList;
import java.util.List;

public class World {

    private final int width;
    private final int height;
    private final TileMap map;
    private final List<Entity> entities = new ArrayList<>();
    private Player player;

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public World(int width, int height, TileMap map) {
        this.width = width;
        this.height = height;
        this.map = map;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public TileMap getMap() {
        return map;
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void update(double deltaTime) {

        for (Entity e : entities) {
            e.update(this, deltaTime);

        }
    }

    public boolean isColliding(Entity e) {

        int tileSize = map.getTileSize();

        int leftTile   = (int) (e.getX() / tileSize);
        int rightTile  = (int) ((e.getX() + e.getSize() - 1) / tileSize);
        int topTile    = (int) (e.getY() / tileSize);
        int bottomTile = (int) ((e.getY() + e.getSize() - 1) / tileSize);

        return map.isWall(topTile, leftTile)
                || map.isWall(topTile, rightTile)
                || map.isWall(bottomTile, leftTile)
                || map.isWall(bottomTile, rightTile);
    }
}