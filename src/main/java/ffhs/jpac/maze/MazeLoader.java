package ffhs.jpac.maze;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
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
        if (pattern.size() != MAZE_HEIGHT) {
            throw new IllegalArgumentException(
                    "Maze height must be exactly " + MAZE_HEIGHT + " rows"
            );
        }

        int playerSpawns = 0;
        int ghostSpawns = 0;

        for (int row = 0; row < pattern.size(); row++) {
            String line = pattern.get(row);
            if (line == null || line.length() != MAZE_WIDTH) {
                throw new IllegalArgumentException(
                        "Maze row " + row + " must be exactly "
                                + MAZE_WIDTH + " characters wide"
                );
            }

            for (int col = 0; col < line.length(); col++) {
                char symbol = line.charAt(col);
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

                if (symbol == 'P') {
                    playerSpawns++;
                } else if (symbol == 'G') {
                    ghostSpawns++;
                }
            }
        }

        if (playerSpawns != 1) {
            throw new IllegalArgumentException(
                    "Maze must contain exactly one player spawn P"
            );
        }

        if (ghostSpawns < 4) {
            throw new IllegalArgumentException(
                    "Maze must contain at least four ghost spawns G"
            );
        }

        validateConnectedPaths(pattern);
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

            for (MazePosition neighbor : List.of(
                    new MazePosition(current.row() - 1, current.col()),
                    new MazePosition(current.row() + 1, current.col()),
                    new MazePosition(current.row(), current.col() - 1),
                    new MazePosition(current.row(), current.col() + 1)
            )) {
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
        return position.row() >= 0
                && position.row() < MAZE_HEIGHT
                && position.col() >= 0
                && position.col() < MAZE_WIDTH
                && pattern.get(position.row()).charAt(position.col()) != '#';
    }
}
