package ffhs.jpac.world;

import ffhs.jpac.maze.MazePosition;

/**
 * Zielstrategie des roten Geistes: direkte Verfolgung des Spielers.
 */
class RedTargetStrategy implements GhostTargetStrategy {

    /**
     * Liefert die aktuelle Spielerkachel als Ziel.
     *
     * @param ghost steuernder Geist
     * @param world aktuelle Spielwelt
     * @return aktuelle Kachel des Spielers
     */
    @Override
    public MazePosition getTarget(
            Ghost ghost,
            World world
    ) {
        return ghost.getPlayerTile(world);
    }
}
