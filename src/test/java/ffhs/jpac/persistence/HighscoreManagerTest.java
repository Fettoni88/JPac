package ffhs.jpac.persistence;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HighscoreManagerTest {

    @Test
    void missingFileReturnsEmptyList() throws IOException {
        Path directory = Files.createTempDirectory("jpac-highscores");
        Path file = directory.resolve("missing.json");
        HighscoreManager manager = new HighscoreManager(file);

        assertTrue(manager.loadHighscores().isEmpty());
    }

    @Test
    void invalidJsonReturnsEmptyList() throws IOException {
        Path file = Files.createTempFile("jpac-invalid-highscores", ".json");
        Files.writeString(file, "not valid json");
        HighscoreManager manager = new HighscoreManager(file);

        assertTrue(manager.loadHighscores().isEmpty());
    }

    @Test
    void addScoreKeepsTopTenSortedDescending() throws IOException {
        Path file = Files.createTempFile("jpac-highscores", ".json");
        Files.deleteIfExists(file);
        HighscoreManager manager = new HighscoreManager(file);

        for (int score = 0; score <= 110; score += 10) {
            manager.addScore("Player " + score, score);
        }

        List<HighscoreEntry> highscores = manager.loadHighscores();

        assertEquals(10, highscores.size());
        assertEquals(110, highscores.get(0).getScore());
        assertEquals(20, highscores.get(9).getScore());
    }
}
