package ffhs.jpac.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Lädt, sortiert und speichert die lokale Highscore-Liste als JSON-Datei.
 *
 * <p>Persistenzfehler werden bewusst fehlertolerant behandelt, damit eine
 * beschädigte oder nicht beschreibbare Datei das Spiel nicht beendet.</p>
 */
public class HighscoreManager {

    private static final int MAX_HIGHSCORES = 10;
    private static final Type HIGHSCORE_LIST_TYPE =
            new TypeToken<List<HighscoreEntry>>() { }.getType();

    private final Path filePath;
    private final Gson gson;

    /**
     * Erstellt einen Manager für die Datei {@code highscores.json} im
     * aktuellen Arbeitsverzeichnis.
     */
    public HighscoreManager() {
        this(Path.of("highscores.json"));
    }

    /**
     * Erstellt einen Manager für einen frei wählbaren Dateipfad.
     *
     * @param filePath Pfad der JSON-Datei
     */
    public HighscoreManager(Path filePath) {
        this.filePath = filePath;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Lädt die gespeicherten Highscores.
     *
     * @return absteigend sortierte Liste mit höchstens zehn Einträgen; bei
     *         fehlender oder ungültiger Datei eine leere Liste
     */
    public List<HighscoreEntry> loadHighscores() {
        try {
            if (!Files.exists(filePath)) {
                return new ArrayList<>();
            }

            try (Reader reader = Files.newBufferedReader(filePath)) {
                List<HighscoreEntry> entries = gson.fromJson(
                        reader,
                        HIGHSCORE_LIST_TYPE
                );
                return prepareHighscores(entries);
            }
        } catch (IOException | JsonParseException | SecurityException exception) {
            return new ArrayList<>();
        }
    }

    /**
     * Speichert eine normalisierte Highscore-Liste.
     *
     * @param entries zu speichernde Einträge
     */
    public void saveHighscores(List<HighscoreEntry> entries) {
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            gson.toJson(prepareHighscores(entries), writer);
        } catch (IOException | SecurityException exception) {
            // A highscore error must not stop the game.
        }
    }

    /**
     * Fügt einen Punktestand ohne Labyrinthzuordnung hinzu.
     *
     * @param name Name des Spielers
     * @param score erreichte Punktzahl
     * @return aktualisierte, sortierte Highscore-Liste
     */
    public List<HighscoreEntry> addScore(String name, int score) {
        return addScore(name, score, "", "Unknown Maze");
    }

    /**
     * Fügt einen Punktestand mit Labyrinthzuordnung hinzu und speichert ihn.
     *
     * @param name Name des Spielers
     * @param score erreichte Punktzahl
     * @param mazeId technische Labyrinthkennung
     * @param mazeName Anzeigename des Labyrinths
     * @return aktualisierte, sortierte Highscore-Liste
     */
    public List<HighscoreEntry> addScore(
            String name,
            int score,
            String mazeId,
            String mazeName
    ) {
        List<HighscoreEntry> entries = loadHighscores();
        entries.add(new HighscoreEntry(name, score, mazeId, mazeName));

        List<HighscoreEntry> preparedEntries = prepareHighscores(entries);
        saveHighscores(preparedEntries);
        return preparedEntries;
    }

    private List<HighscoreEntry> prepareHighscores(
            List<HighscoreEntry> entries
    ) {
        if (entries == null) {
            return new ArrayList<>();
        }

        return entries.stream()
                .filter(entry -> entry != null && entry.getName() != null)
                .sorted(Comparator.comparingInt(HighscoreEntry::getScore)
                        .reversed())
                .limit(MAX_HIGHSCORES)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}
