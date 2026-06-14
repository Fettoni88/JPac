package ffhs.jpac.maze;

import java.util.List;

public class MazeData {

    private String name;
    private List<String> pattern;

    public MazeData(String name, List<String> pattern) {
        this.name = name;
        this.pattern = pattern;
    }

    public String getName() {
        return name;
    }

    public List<String> getPattern() {
        return pattern;
    }
}
