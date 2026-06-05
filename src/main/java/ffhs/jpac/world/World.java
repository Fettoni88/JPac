package ffhs.jpac.world;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.List;

public class World {

    private final int width;
    private final int height;
    private final TileMap map;
    private final List<Entity> entities = new ArrayList<>();
    private final List<Pellet> pellets = new ArrayList<>();
    private Player player;
    private int score = 0;
    private GameState gameState = GameState.START;

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

    public GameState getGameState() {
        return gameState;
    }

    public void startGame() {
        if (gameState == GameState.START) {
            gameState = GameState.RUNNING;
        }
    }

    public void restartGame() {
        score = 0;

        for (Entity entity : entities) {
            entity.reset();
        }

        generatePellets();
        gameState = GameState.START;
    }

    public void update(double deltaTime) {

        if (gameState != GameState.RUNNING) {
            return;
        }

        for (Entity e : entities) {
            e.update(this, deltaTime);
        }

        checkPelletCollection();
        checkGhostCollision();

        if (gameState == GameState.RUNNING && areAllPelletsCollected()) {
            gameState = GameState.WIN;
        }
    }

    public boolean isGameOver() {
        return gameState == GameState.GAME_OVER;
    }

    public boolean isColliding(Entity e) {

        int tileSize = map.getTileSize();

        int leftTile   = (int) (e.getX() / tileSize);
        int rightTile  = (int) ((e.getX() + e.getSize() - 1) / tileSize);
        int topTile    = (int) (e.getY() / tileSize);
        int bottomTile = (int) ((e.getY() + e.getSize() - 1) / tileSize);

        return isBlockedFor(e, topTile, leftTile)
                || isBlockedFor(e, topTile, rightTile)
                || isBlockedFor(e, bottomTile, leftTile)
                || isBlockedFor(e, bottomTile, rightTile);
    }

    public boolean canMove(Entity entity, Direction direction) {
        return canMove(entity, direction, 2);
    }

    public boolean canMove(Entity entity, Direction direction, double distance) {
        if (direction == Direction.NONE) {
            return false;
        }

        double oldX = entity.getX();
        double oldY = entity.getY();

        double testX = oldX + direction.getDx() * distance;
        double testY = oldY + direction.getDy() * distance;

        entity.setX(testX);
        entity.setY(testY);

        boolean canMove = !isColliding(entity);

        entity.setX(oldX);
        entity.setY(oldY);

        return canMove;
    }

    private boolean isBlockedFor(Entity entity, int row, int col) {
        if (!map.isInside(row, col)) {
            return true;
        }

        if (map.isWall(row, col)) {
            return true;
        }

        if (!map.isGhostHouse(row, col)) {
            return false;
        }

        if (entity instanceof Player) {
            return true;
        }

        return entity instanceof Ghost ghost && ghost.hasLeftGhostHouse();
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

    private void checkGhostCollision() {
        if (player == null) {
            return;
        }

        for (Entity entity : entities) {
            if (entity instanceof Ghost ghost
                    && ghost.isReleased()
                    && isOverlapping(player, ghost)) {
                gameState = GameState.GAME_OVER;
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
        pellets.clear();

        if (player == null) {
            return;
        }

        int tileSize = map.getTileSize();
        int startCol = (int) ((player.getX() + player.getSize() / 2.0) / tileSize);
        int startRow = (int) ((player.getY() + player.getSize() / 2.0) / tileSize);
        boolean[][] visited = new boolean[map.getRows()][map.getCols()];
        Queue<int[]> openTiles = new ArrayDeque<>();

        visited[startRow][startCol] = true;
        openTiles.add(new int[]{startRow, startCol});

        while (!openTiles.isEmpty()) {
            int[] tile = openTiles.remove();
            int row = tile[0];
            int col = tile[1];

            if (map.isPelletTile(row, col)) {
                double x = col * tileSize + tileSize / 2.0 - 3;
                double y = row * tileSize + tileSize / 2.0 - 3;
                pellets.add(new Pellet(x, y));
            }

            for (Direction direction : List.of(
                    Direction.UP,
                    Direction.DOWN,
                    Direction.LEFT,
                    Direction.RIGHT
            )) {
                int nextRow = row + direction.getDy();
                int nextCol = col + direction.getDx();

                if (map.isInside(nextRow, nextCol)
                        && !visited[nextRow][nextCol]
                        && map.isPelletTile(nextRow, nextCol)) {
                    visited[nextRow][nextCol] = true;
                    openTiles.add(new int[]{nextRow, nextCol});
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
