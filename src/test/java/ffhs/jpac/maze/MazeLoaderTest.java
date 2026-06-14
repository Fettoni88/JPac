package ffhs.jpac.maze;

import ffhs.jpac.world.TileMap;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MazeLoaderTest {

    @Test
    void allFiveMazesLoadWithRequiredDimensionsAndSpawns() {
        for (int mazeNumber = 1; mazeNumber <= 5; mazeNumber++) {
            MazeData mazeData = MazeLoader.load(
                    "/mazes/maze" + mazeNumber + ".json"
            );
            TileMap tileMap = new TileMap(mazeData);

            assertEquals(MazeLoader.MAZE_HEIGHT, tileMap.getRows());
            assertEquals(MazeLoader.MAZE_WIDTH, tileMap.getCols());
            assertEquals(24, tileMap.getTileSize());
            assertEquals(1, countSymbol(mazeData, 'P'));
            assertTrue(tileMap.getGhostSpawns().size() >= 4);
            assertFalse(tileMap.getGhostHouseExits().isEmpty());
            assertEquals("maze" + mazeNumber, tileMap.getMazeId());
            assertEquals("Maze " + mazeNumber, tileMap.getMazeName());
            assertFalse(tileMap.getPelletPositions().isEmpty());
            assertFalse(
                    tileMap.getPelletPositions().contains(
                            tileMap.getPlayerSpawn()
                    )
            );
            assertTrue(
                    tileMap.getGhostSpawns().stream()
                            .noneMatch(tileMap.getPelletPositions()::contains)
            );
        }
    }

    @Test
    void invalidHeightThrowsMeaningfulException() {
        List<String> pattern = createValidPattern();
        pattern.removeLast();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> MazeLoader.validate(new MazeData("Short", pattern))
        );

        assertTrue(exception.getMessage().contains("height"));
    }

    @Test
    void invalidWidthThrowsMeaningfulException() {
        List<String> pattern = createValidPattern();
        pattern.set(10, pattern.get(10).substring(1));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> MazeLoader.validate(new MazeData("Narrow", pattern))
        );

        assertTrue(exception.getMessage().contains("wide"));
    }

    @Test
    void missingPlayerSpawnThrowsMeaningfulException() {
        List<String> pattern = createValidPattern();
        pattern.set(2, replaceSymbol(pattern.get(2), 2, '.'));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> MazeLoader.validate(
                        new MazeData("No player", pattern)
                )
        );

        assertTrue(exception.getMessage().contains("player spawn"));
    }

    @Test
    void missingGhostSpawnsThrowsMeaningfulException() {
        List<String> pattern = createValidPattern();
        for (int row = 0; row < pattern.size(); row++) {
            pattern.set(row, pattern.get(row).replace('G', 'H'));
        }

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> MazeLoader.validate(
                        new MazeData("No ghosts", pattern)
                )
        );

        assertTrue(exception.getMessage().contains("ghost spawns"));
    }

    @Test
    void missingGhostHouseExitThrowsMeaningfulException() {
        List<String> pattern = createValidPattern();
        pattern.set(3, pattern.get(3).replace('E', '.'));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> MazeLoader.validate(
                        new MazeData("No exit", pattern)
                )
        );

        assertTrue(exception.getMessage().contains("exit"));
    }

    private List<String> createValidPattern() {
        List<String> pattern = new ArrayList<>();
        pattern.add("#".repeat(MazeLoader.MAZE_WIDTH));

        for (int row = 1; row < MazeLoader.MAZE_HEIGHT - 1; row++) {
            pattern.add("#" + ".".repeat(MazeLoader.MAZE_WIDTH - 2) + "#");
        }

        pattern.add("#".repeat(MazeLoader.MAZE_WIDTH));
        pattern.set(2, replaceSymbol(pattern.get(2), 2, 'P'));
        pattern.set(3, replaceSection(pattern.get(3), 9, "##E###"));
        pattern.set(4, replaceSection(pattern.get(4), 9, "#HHHH#"));
        pattern.set(5, replaceSection(pattern.get(5), 9, "#HGGH#"));
        pattern.set(6, replaceSection(pattern.get(6), 9, "#HGGH#"));
        pattern.set(7, replaceSection(pattern.get(7), 9, "######"));

        return pattern;
    }

    private String replaceSection(String row, int start, String section) {
        StringBuilder changedRow = new StringBuilder(row);
        changedRow.replace(start, start + section.length(), section);
        return changedRow.toString();
    }

    private String replaceSymbol(String row, int col, char symbol) {
        StringBuilder changedRow = new StringBuilder(row);
        changedRow.setCharAt(col, symbol);
        return changedRow.toString();
    }

    private long countSymbol(MazeData mazeData, char symbol) {
        return mazeData.getPattern().stream()
                .flatMapToInt(String::chars)
                .filter(character -> character == symbol)
                .count();
    }
}
