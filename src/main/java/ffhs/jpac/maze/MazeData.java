package ffhs.jpac.maze;

import java.util.List;

public class MazeData {

    private String id;
    private String name;
    private List<String> pattern;

    public MazeData(String name, List<String> pattern) {
        this("", name, pattern);
    }

    public MazeData(String id, String name, List<String> pattern) {
        this.id = id;
        this.name = name;
        this.pattern = pattern;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getPattern() {
        return pattern;
    }
}
