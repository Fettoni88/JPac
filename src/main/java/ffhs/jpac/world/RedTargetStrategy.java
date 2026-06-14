package ffhs.jpac.world;

import ffhs.jpac.maze.MazePosition;

class RedTargetStrategy implements GhostTargetStrategy {

    @Override
    public MazePosition getTarget(
            Ghost ghost,
            World world
    ) {
        return ghost.getPlayerTile(world);
    }
}
