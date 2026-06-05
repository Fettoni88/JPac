package ffhs.jpac.world;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TileMap {

    private static final int TILE_SIZE = 32;

    private static final int FLOOR = 0;
    private static final int WALL = 1;
    private static final int GHOST_HOUSE = 2;

    private final TileType[][] map;

    public TileMap(String resourcePath) {
        this.map = loadMap(resourcePath);
    }

    private TileType[][] loadMap(String path) {

        try {
            InputStream is = getClass().getResourceAsStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            List<TileType[]> rows = new ArrayList<>();
            String line;

            while ((line = br.readLine()) != null) {

                String[] tokens = line.trim().split("\\s+");
                TileType[] row = new TileType[tokens.length];

                for (int i = 0; i < tokens.length; i++) {

                    int value = Integer.parseInt(tokens[i]);

                    if (value == WALL) {
                        row[i] = TileType.WALL;
                    } else if (value == GHOST_HOUSE) {
                        row[i] = TileType.GHOST_HOUSE;
                    } else {
                        row[i] = TileType.FLOOR;
                    }
                }

                rows.add(row);
            }

            TileType[][] result = new TileType[rows.size()][];

            for (int i = 0; i < rows.size(); i++) {
                result[i] = rows.get(i);
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Map konnte nicht geladen werden: " + path, e);
        }
    }

    public TileType getTile(int row, int col) {
        return map[row][col];
    }

    public boolean isWall(int row, int col) {
        return map[row][col].isSolid();
    }

    public boolean isGhostHouse(int row, int col) {
        return map[row][col] == TileType.GHOST_HOUSE;
    }

    public boolean isPelletTile(int row, int col) {
        return map[row][col] == TileType.FLOOR;
    }

    public boolean isInside(int row, int col) {
        return row >= 0 && col >= 0 && row < getRows() && col < getCols();
    }

    public int getRows() {
        return map.length;
    }

    public int getCols() {
        return map[0].length;
    }

    public int getTileSize() {
        return TILE_SIZE;
    }

    public int[] findGhostHouseExit() {
        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getCols(); col++) {
                if (!isGhostHouse(row, col)) {
                    continue;
                }

                for (Direction direction : List.of(
                        Direction.UP,
                        Direction.DOWN,
                        Direction.LEFT,
                        Direction.RIGHT
                )) {
                    int outsideRow = row + direction.getDy();
                    int outsideCol = col + direction.getDx();

                    if (isInside(outsideRow, outsideCol)
                            && getTile(outsideRow, outsideCol) == TileType.FLOOR) {
                        return new int[]{row, col, outsideRow, outsideCol};
                    }
                }
            }
        }

        throw new IllegalStateException("Ghost house has no exit");
    }
}
