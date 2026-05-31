package ffhs.jpac.world;

import java.util.ArrayList;
import java.util.List;

public class World {

    private final int width;
    private final int height;
    private final TileMap map;
    private final List<Entity> entities = new ArrayList<>();
    private final List<Pellet> pellets = new ArrayList<>();
    private Player player;
    private int score = 0;

    public int getScore() {
        return score;
    }

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

    public void addPellet(Pellet pellet) {
        pellets.add(pellet);
    }

    public List<Pellet> getPellets() {
        return pellets;
    }

    public void update(double deltaTime) {

        checkPelletCollection();

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

    public boolean canMove(Entity entity, Direction direction) {

        if (direction == Direction.NONE) {
            return false;
        }

        double oldX = entity.getX();
        double oldY = entity.getY();

        double testX = oldX + direction.getDx() * 2;
        double testY = oldY + direction.getDy() * 2;

        entity.setX(testX);
        entity.setY(testY);

        boolean canMove = !isColliding(entity);

        entity.setX(oldX);
        entity.setY(oldY);

        return canMove;
    }

    private void checkPelletCollection() {
        if (player == null) {
            return;
        }

        for (Pellet pellet : pellets) {
            if (!pellet.isCollected() && isOverlapping(player, pellet)) {
                pellet.collect();
                score += 10;
            }
        }
    }

    private boolean isOverlapping(Entity a, Entity b) {
        return a.getX() < b.getX() + b.getSize()
                && a.getX() + a.getSize() > b.getX()
                && a.getY() < b.getY() + b.getSize()
                && a.getY() + a.getSize() > b.getY();
    }

    public void generatePellets() {
        int tileSize = map.getTileSize();

        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {

                if (!map.isWall(row, col)) {
                    double x = col * tileSize + tileSize / 2.0 - 3;
                    double y = row * tileSize + tileSize / 2.0 - 3;

                    pellets.add(new Pellet(x, y));
                }
            }
        }
    }

    public boolean areAllPelletsCollected() {
        for (Pellet pellet : pellets) {
            if (!pellet.isCollected()) {
                return false;
            }
        }
        return true;
    }
}