package ffhs.jpac.world;

import ffhs.jpac.maze.MazeData;
import ffhs.jpac.maze.MazeLoader;
import ffhs.jpac.maze.MazePosition;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TileMap {

    private static final int TILE_SIZE = 24;

    private static final int FLOOR = 0;
    private static final int WALL = 1;
    private static final int GHOST_HOUSE = 2;

    private final TileType[][] map;
    private final boolean[][] pelletTiles;
    private final List<MazePosition> pelletPositions = new ArrayList<>();
    private final List<MazePosition> ghostSpawns = new ArrayList<>();
    private final List<MazePosition> ghostHouseExits = new ArrayList<>();
    private String mazeId = "legacy";
    private String mazeName = "Legacy Maze";
    private MazePosition playerSpawn;

    public TileMap(String resourcePath) {
        if (resourcePath.endsWith(".json")) {
            loadMazeData(MazeLoader.load(resourcePath));
        } else {
            loadLegacyMap(resourcePath);
        }

        map = loadedMap;
        pelletTiles = loadedPelletTiles;
    }

    public TileMap(MazeData mazeData) {
        MazeLoader.validate(mazeData);
        loadMazeData(mazeData);
        map = loadedMap;
        pelletTiles = loadedPelletTiles;
    }

    private TileType[][] loadedMap;
    private boolean[][] loadedPelletTiles;

    private void loadMazeData(MazeData mazeData) {
        mazeId = mazeData.getId();
        mazeName = mazeData.getName();
        List<String> pattern = mazeData.getPattern();
        loadedMap = new TileType[pattern.size()][pattern.getFirst().length()];
        loadedPelletTiles =
                new boolean[pattern.size()][pattern.getFirst().length()];

        for (int row = 0; row < pattern.size(); row++) {
            for (int col = 0; col < pattern.get(row).length(); col++) {
                char symbol = pattern.get(row).charAt(col);
                loadedMap[row][col] = switch (symbol) {
                    case '#' -> TileType.WALL;
                    case 'G', 'H' -> TileType.GHOST_HOUSE;
                    default -> TileType.FLOOR;
                };

                if (symbol == '.' || symbol == 'o') {
                    loadedPelletTiles[row][col] = true;
                    pelletPositions.add(new MazePosition(row, col));
                } else if (symbol == 'P') {
                    playerSpawn = new MazePosition(row, col);
                } else if (symbol == 'G') {
                    ghostSpawns.add(new MazePosition(row, col));
                } else if (symbol == 'E') {
                    ghostHouseExits.add(new MazePosition(row, col));
                }
            }
        }
    }

    private void loadLegacyMap(String resourcePath) {
        try {
            InputStream input = getClass().getResourceAsStream(resourcePath);
            if (input == null) {
                throw new IllegalArgumentException(
                        "Map resource not found: " + resourcePath
                );
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input)
            );
            List<TileType[]> rows = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");
                TileType[] row = new TileType[tokens.length];

                for (int col = 0; col < tokens.length; col++) {
                    int value = Integer.parseInt(tokens[col]);
                    row[col] = switch (value) {
                        case WALL -> TileType.WALL;
                        case GHOST_HOUSE -> TileType.GHOST_HOUSE;
                        default -> TileType.FLOOR;
                    };
                }

                rows.add(row);
            }

            loadedMap = rows.toArray(TileType[][]::new);
            loadedPelletTiles =
                    new boolean[loadedMap.length][loadedMap[0].length];

            for (int row = 0; row < loadedMap.length; row++) {
                for (int col = 0; col < loadedMap[row].length; col++) {
                    if (loadedMap[row][col] == TileType.FLOOR) {
                        loadedPelletTiles[row][col] = true;
                        pelletPositions.add(new MazePosition(row, col));
                    }
                }
            }
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Could not load map: " + resourcePath,
                    exception
            );
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

    public boolean hasGhostHouse() {
        for (TileType[] row : map) {
            for (TileType tile : row) {
                if (tile == TileType.GHOST_HOUSE) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isPelletTile(int row, int col) {
        return pelletTiles[row][col];
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

    public int getTileColFromPixel(double pixelX) {
        return (int) Math.floor(pixelX / TILE_SIZE);
    }

    public int getTileRowFromPixel(double pixelY) {
        return (int) Math.floor(pixelY / TILE_SIZE);
    }

    public double getTileCenterX(int col) {
        return col * TILE_SIZE + TILE_SIZE / 2.0;
    }

    public double getTileCenterY(int row) {
        return row * TILE_SIZE + TILE_SIZE / 2.0;
    }

    public boolean isWalkableForPlayer(int row, int col) {
        return isInside(row, col)
                && !isWall(row, col)
                && !isGhostHouse(row, col);
    }

    public String getMazeId() {
        return mazeId;
    }

    public String getMazeName() {
        return mazeName;
    }

    public MazePosition getPlayerSpawn() {
        if (playerSpawn == null) {
            throw new IllegalStateException("Map has no player spawn");
        }
        return playerSpawn;
    }

    public List<MazePosition> getGhostSpawns() {
        return List.copyOf(ghostSpawns);
    }

    public List<MazePosition> getPelletPositions() {
        return List.copyOf(pelletPositions);
    }

    public List<MazePosition> getGhostHouseExits() {
        return List.copyOf(ghostHouseExits);
    }

    public int[] findGhostHouseExit() {
        int[] center = findGhostHouseCenter();
        return findGhostHouseExit(
                getTileCenterX(center[1]),
                getTileCenterY(center[0])
        );
    }

    public int[] findGhostHouseExit(double pixelX, double pixelY) {
        if (!ghostHouseExits.isEmpty()) {
            int startRow = getTileRowFromPixel(pixelY);
            int startCol = getTileColFromPixel(pixelX);
            MazePosition exit = ghostHouseExits.stream()
                    .min((first, second) -> Integer.compare(
                            distance(startRow, startCol, first),
                            distance(startRow, startCol, second)
                    ))
                    .orElseThrow();

            for (Direction direction : List.of(
                    Direction.UP,
                    Direction.DOWN,
                    Direction.LEFT,
                    Direction.RIGHT
            )) {
                int houseRow = exit.row() - direction.getDy();
                int houseCol = exit.col() - direction.getDx();

                if (isInside(houseRow, houseCol)
                        && isGhostHouse(houseRow, houseCol)) {
                    return new int[]{
                            houseRow,
                            houseCol,
                            exit.row(),
                            exit.col()
                    };
                }
            }

            throw new IllegalStateException(
                    "Ghost house exit is not next to ghost house floor"
            );
        }

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
                            && getTile(outsideRow, outsideCol)
                            == TileType.FLOOR) {
                        return new int[]{
                                row,
                                col,
                                outsideRow,
                                outsideCol
                        };
                    }
                }
            }
        }

        throw new IllegalStateException("Ghost house has no exit");
    }

    private int distance(
            int startRow,
            int startCol,
            MazePosition position
    ) {
        return Math.abs(startRow - position.row())
                + Math.abs(startCol - position.col());
    }

    public int[] findGhostHouseCenter() {
        int rowSum = 0;
        int colSum = 0;
        int tileCount = 0;

        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getCols(); col++) {
                if (isGhostHouse(row, col)) {
                    rowSum += row;
                    colSum += col;
                    tileCount++;
                }
            }
        }

        if (tileCount == 0) {
            throw new IllegalStateException("Map has no ghost house");
        }

        return new int[]{
                Math.round((float) rowSum / tileCount),
                Math.round((float) colSum / tileCount)
        };
    }
}
