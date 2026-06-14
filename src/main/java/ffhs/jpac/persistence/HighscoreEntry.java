package ffhs.jpac.persistence;

public class HighscoreEntry {

    private String name;
    private int score;
    private String mazeId;
    private String mazeName;

    public HighscoreEntry(String name, int score) {
        this(name, score, "", "Unknown Maze");
    }

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

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public String getMazeId() {
        return mazeId == null ? "" : mazeId;
    }

    public String getMazeName() {
        if (mazeName == null || mazeName.isBlank()) {
            return "Unknown Maze";
        }
        return mazeName;
    }
}
