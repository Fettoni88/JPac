package ffhs.jpac.maze;

import java.util.List;

/**
 * Repräsentiert die aus einer JSON-Datei gelesenen Rohdaten eines Labyrinths.
 */
public class MazeData {

    private String id;
    private String name;
    private List<String> pattern;

    /**
     * Erstellt Labyrinthdaten ohne explizite technische Kennung.
     *
     * @param name Anzeigename des Labyrinths
     * @param pattern zeilenweises Zeichenmuster des Labyrinths
     */
    public MazeData(String name, List<String> pattern) {
        this("", name, pattern);
    }

    /**
     * Erstellt vollständige Labyrinthdaten.
     *
     * @param id technische Kennung des Labyrinths
     * @param name Anzeigename des Labyrinths
     * @param pattern zeilenweises Zeichenmuster des Labyrinths
     */
    public MazeData(String id, String name, List<String> pattern) {
        this.id = id;
        this.name = name;
        this.pattern = pattern;
    }

    /**
     * Gibt die technische Kennung zurück.
     *
     * @return Labyrinthkennung
     */
    public String getId() {
        return id;
    }

    /**
     * Gibt den Anzeigenamen zurück.
     *
     * @return Name des Labyrinths
     */
    public String getName() {
        return name;
    }

    /**
     * Gibt das zeilenweise Labyrinthmuster zurück.
     *
     * @return Liste der Musterzeilen
     */
    public List<String> getPattern() {
        return pattern;
    }
}
