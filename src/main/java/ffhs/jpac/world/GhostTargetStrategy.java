package ffhs.jpac.world;

import ffhs.jpac.maze.MazePosition;

interface GhostTargetStrategy {

    MazePosition getTarget(Ghost ghost, World world);

    default void update(double deltaTime) {
    }

    default void reset() {
    }
}
