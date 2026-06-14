package ffhs.jpac.maze;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public final class MazeLoader {

    public static final int MAZE_WIDTH = 24;
    public static final int MAZE_HEIGHT = 32;

    private static final Set<Character> SUPPORTED_SYMBOLS = Set.of(
            '#',
            '.',
            ' ',
            'P',
            'G',
            'H',
            'E',
            'o'
    );

    private MazeLoader() {
    }

    public static MazeData load(String resourcePath) {
        InputStream input = MazeLoader.class.getResourceAsStream(resourcePath);
        if (input == null) {
            throw new IllegalArgumentException(
                    "Maze resource not found: " + resourcePath
            );
        }

        try (Reader reader = new InputStreamReader(
                input,
                StandardCharsets.UTF_8
        )) {
            MazeData mazeData = new Gson().fromJson(reader, MazeData.class);
            validate(mazeData);
            return mazeData;
        } catch (IOException | JsonParseException exception) {
            throw new IllegalArgumentException(
                    "Could not load maze: " + resourcePath,
                    exception
            );
        }
    }

    public static void validate(MazeData mazeData) {
        if (mazeData == null || mazeData.getPattern() == null) {
            throw new IllegalArgumentException("Maze pattern is missing");
        }

        List<String> pattern = mazeData.getPattern();
        validateDimensions(pattern);
        SpawnCounts spawnCounts = validateSymbolsAndCountSpawns(pattern);
        validateRequiredSpawns(spawnCounts);
        validateGhostHouse(pattern);
        validateConnectedPaths(pattern);
        validatePlayerPaths(pattern);
    }

    private static void validateDimensions(List<String> pattern) {
        if (pattern.size() != MAZE_HEIGHT) {
            throw new IllegalArgumentException(
                    "Maze height must be exactly " + MAZE_HEIGHT + " rows"
            );
        }

        for (int row = 0; row < pattern.size(); row++) {
            String line = pattern.get(row);
            if (line == null || line.length() != MAZE_WIDTH) {
                throw new IllegalArgumentException(
                        "Maze row " + row + " must be exactly "
                                + MAZE_WIDTH + " characters wide"
                );
            }
        }
    }

    private static SpawnCounts validateSymbolsAndCountSpawns(
            List<String> pattern
    ) {
        int playerSpawns = 0;
        int ghostSpawns = 0;
        int exits = 0;

        for (int row = 0; row < pattern.size(); row++) {
            String line = pattern.get(row);
            for (int col = 0; col < line.length(); col++) {
                char symbol = line.charAt(col);
                validateSymbol(symbol, row, col);

                if (symbol == 'P') {
                    playerSpawns++;
                } else if (symbol == 'G') {
                    ghostSpawns++;
                } else if (symbol == 'E') {
                    exits++;
                }
            }
        }

        return new SpawnCounts(playerSpawns, ghostSpawns, exits);
    }

    private static void validateSymbol(char symbol, int row, int col) {
        if (!SUPPORTED_SYMBOLS.contains(symbol)) {
            throw new IllegalArgumentException(
                    "Unsupported maze symbol '" + symbol
                            + "' at row " + row + ", col " + col
            );
        }

        if (isBorder(row, col) && symbol != '#') {
            throw new IllegalArgumentException(
                    "Maze must be enclosed by walls"
            );
        }
    }

    private static void validateRequiredSpawns(SpawnCounts counts) {
        if (counts.playerSpawns() != 1) {
            throw new IllegalArgumentException(
                    "Maze must contain exactly one player spawn P"
            );
        }

        if (counts.ghostSpawns() < 4) {
            throw new IllegalArgumentException(
                    "Maze must contain at least four ghost spawns G"
            );
        }

        if (counts.exits() < 1) {
            throw new IllegalArgumentException(
                    "Maze must contain at least one ghost house exit E"
            );
        }
    }

    private static boolean isBorder(int row, int col) {
        return row == 0
                || row == MAZE_HEIGHT - 1
                || col == 0
                || col == MAZE_WIDTH - 1;
    }

    private static void validateConnectedPaths(List<String> pattern) {
        MazePosition start = findFirstPathTile(pattern);
        Set<MazePosition> visited = new HashSet<>();
        Queue<MazePosition> open = new ArrayDeque<>();
        visited.add(start);
        open.add(start);

        while (!open.isEmpty()) {
            MazePosition current = open.remove();

            for (MazePosition neighbor : neighborsOf(current)) {
                if (isPathTile(pattern, neighbor)
                        && visited.add(neighbor)) {
                    open.add(neighbor);
                }
            }
        }

        int pathTileCount = 0;
        for (String row : pattern) {
            for (int col = 0; col < row.length(); col++) {
                if (row.charAt(col) != '#') {
                    pathTileCount++;
                }
            }
        }

        if (visited.size() != pathTileCount) {
            throw new IllegalArgumentException(
                    "All maze paths and spawn points must be connected"
            );
        }
    }

    private static void validateGhostHouse(List<String> pattern) {
        List<MazePosition> houseTiles = new ArrayList<>();

        for (int row = 0; row < pattern.size(); row++) {
            for (int col = 0; col < pattern.get(row).length(); col++) {
                char symbol = pattern.get(row).charAt(col);
                if (symbol == 'H' || symbol == 'G') {
                    houseTiles.add(new MazePosition(row, col));
                }

                if (symbol == 'E'
                        && !hasNeighbor(pattern, row, col, 'H', 'G')) {
                    throw new IllegalArgumentException(
                            "Ghost house exit E must be next to H or G"
                    );
                }

                if (symbol == 'G'
                        && !hasNeighbor(pattern, row, col, 'H', 'G')) {
                    throw new IllegalArgumentException(
                            "Ghost spawn G must be inside the ghost house"
                    );
                }

                if (symbol == 'G'
                        && hasNeighbor(pattern, row, col, 'G', 'G')) {
                    throw new IllegalArgumentException(
                            "Ghost spawn tiles G must not be adjacent"
                    );
                }
            }
        }

        validateSingleGhostHouse(pattern, houseTiles);
    }

    private static void validateSingleGhostHouse(
            List<String> pattern,
            List<MazePosition> houseTiles
    ) {
        if (houseTiles.isEmpty()) {
            throw new IllegalArgumentException("Maze has no ghost house");
        }

        Set<MazePosition> visited = new HashSet<>();
        Queue<MazePosition> open = new ArrayDeque<>();
        visited.add(houseTiles.getFirst());
        open.add(houseTiles.getFirst());

        while (!open.isEmpty()) {
            MazePosition current = open.remove();
            for (MazePosition neighbor : neighborsOf(current)) {
                if (isGhostHouseTile(pattern, neighbor)
                        && visited.add(neighbor)) {
                    open.add(neighbor);
                }
            }
        }

        if (visited.size() != houseTiles.size()) {
            throw new IllegalArgumentException(
                    "Maze must contain exactly one connected ghost house"
            );
        }
    }

    private static boolean isGhostHouseTile(
            List<String> pattern,
            MazePosition position
    ) {
        if (!isInsideMaze(position)) {
            return false;
        }

        char symbol = pattern.get(position.row())
                .charAt(position.col());
        return symbol == 'H' || symbol == 'G';
    }

    private static boolean hasNeighbor(
            List<String> pattern,
            int row,
            int col,
            char firstSymbol,
            char secondSymbol
    ) {
        for (MazePosition neighbor : neighborsOf(
                new MazePosition(row, col)
        )) {
            if (!isInsideMaze(neighbor)) {
                continue;
            }

            char symbol = pattern.get(neighbor.row())
                    .charAt(neighbor.col());
            if (symbol == firstSymbol || symbol == secondSymbol) {
                return true;
            }
        }

        return false;
    }

    private static void validatePlayerPaths(List<String> pattern) {
        MazePosition start = findSymbol(pattern, 'P');
        Set<MazePosition> visited = new HashSet<>();
        Queue<MazePosition> open = new ArrayDeque<>();
        visited.add(start);
        open.add(start);

        while (!open.isEmpty()) {
            MazePosition current = open.remove();
            for (MazePosition neighbor : neighborsOf(current)) {
                if (isPlayerPathTile(pattern, neighbor)
                        && visited.add(neighbor)) {
                    open.add(neighbor);
                }
            }
        }

        int playerPathCount = 0;
        for (String row : pattern) {
            for (int col = 0; col < row.length(); col++) {
                char symbol = row.charAt(col);
                if (symbol != '#' && symbol != 'H' && symbol != 'G') {
                    playerPathCount++;
                }
            }
        }

        if (visited.size() != playerPathCount) {
            throw new IllegalArgumentException(
                    "All player paths and pellets must be reachable"
            );
        }
    }

    private static MazePosition findSymbol(
            List<String> pattern,
            char wantedSymbol
    ) {
        for (int row = 0; row < pattern.size(); row++) {
            int col = pattern.get(row).indexOf(wantedSymbol);
            if (col >= 0) {
                return new MazePosition(row, col);
            }
        }

        throw new IllegalArgumentException(
                "Maze is missing symbol " + wantedSymbol
        );
    }

    private static boolean isPlayerPathTile(
            List<String> pattern,
            MazePosition position
    ) {
        if (!isInsideMaze(position)) {
            return false;
        }

        char symbol = pattern.get(position.row())
                .charAt(position.col());
        return symbol != '#' && symbol != 'H' && symbol != 'G';
    }

    private static MazePosition findFirstPathTile(List<String> pattern) {
        for (int row = 0; row < pattern.size(); row++) {
            for (int col = 0; col < pattern.get(row).length(); col++) {
                if (pattern.get(row).charAt(col) != '#') {
                    return new MazePosition(row, col);
                }
            }
        }

        throw new IllegalArgumentException("Maze has no walkable path");
    }

    private static boolean isPathTile(
            List<String> pattern,
            MazePosition position
    ) {
        return isInsideMaze(position)
                && pattern.get(position.row()).charAt(position.col()) != '#';
    }

    private static List<MazePosition> neighborsOf(MazePosition position) {
        return List.of(
                new MazePosition(position.row() - 1, position.col()),
                new MazePosition(position.row() + 1, position.col()),
                new MazePosition(position.row(), position.col() - 1),
                new MazePosition(position.row(), position.col() + 1)
        );
    }

    private static boolean isInsideMaze(MazePosition position) {
        return position.row() >= 0
                && position.row() < MAZE_HEIGHT
                && position.col() >= 0
                && position.col() < MAZE_WIDTH;
    }

    private record SpawnCounts(
            int playerSpawns,
            int ghostSpawns,
            int exits
    ) {
    }
}
