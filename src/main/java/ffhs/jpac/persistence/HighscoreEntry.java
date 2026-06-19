package ffhs.jpac.persistence;

/**
 * Beschreibt einen einzelnen persistenten Highscore-Eintrag.
 */
public class HighscoreEntry {

    private String name;
    private int score;
    private String mazeId;
    private String mazeName;

    /**
     * Erstellt einen Highscore ohne Labyrinthzuordnung.
     *
     * @param name Name des Spielers
     * @param score erreichte Punktzahl
     */
    public HighscoreEntry(String name, int score) {
        this(name, score, "", "Unknown Maze");
    }

    /**
     * Erstellt einen Highscore mit Labyrinthzuordnung.
     *
     * @param name Name des Spielers
     * @param score erreichte Punktzahl
     * @param mazeId technische Kennung des gespielten Labyrinths
     * @param mazeName Anzeigename des gespielten Labyrinths
     */
    public HighscoreEntry(
            String name,
            int score,
            String mazeId,
            String mazeName
    ) {
        this.name = name;
        this.score = score;
        this.mazeId = mazeId;
        this.mazeName = mazeName;
    }

    /**
     * Gibt den Namen des Spielers zurück.
     *
     * @return Spielername
     */
    public String getName() {
        return name;
    }

    /**
     * Gibt die erreichte Punktzahl zurück.
     *
     * @return Punktzahl
     */
    public int getScore() {
        return score;
    }

    /**
     * Gibt die technische Labyrinthkennung zurück.
     *
     * @return Kennung oder eine leere Zeichenkette, falls keine vorhanden ist
     */
    public String getMazeId() {
        return mazeId == null ? "" : mazeId;
    }

    /**
     * Gibt den Anzeigenamen des Labyrinths zurück.
     *
     * @return Labyrinthname oder {@code "Unknown Maze"} bei fehlenden Daten
     */
    public String getMazeName() {
        if (mazeName == null || mazeName.isBlank()) {
            return "Unknown Maze";
        }
        return mazeName;
    }
}
