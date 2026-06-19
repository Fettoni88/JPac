package ffhs.jpac.world;

import ffhs.jpac.maze.MazePosition;

/**
 * Zielstrategie des orangen Geistes mit Verfolgungs- und Fluchtverhalten.
 */
class OrangeTargetStrategy implements GhostTargetStrategy {

    private static final int FLEE_DISTANCE = 8;

    /**
     * Verfolgt einen entfernten Spieler und wählt im Nahbereich ein fernes
     * Patrouillenziel.
     *
     * @param ghost steuernder Geist
     * @param world aktuelle Spielwelt
     * @return aktuelle Zielkachel
     */
    @Override
    public MazePosition getTarget(
            Ghost ghost,
            World world
    ) {
        MazePosition ghostTile = ghost.getCurrentTile(world);
        MazePosition playerTile = ghost.getPlayerTile(world);
        int distance = Math.abs(ghostTile.row() - playerTile.row())
                + Math.abs(ghostTile.col() - playerTile.col());

        if (distance > FLEE_DISTANCE) {
            return playerTile;
        }

        return world.getMap().getFarthestPatrolTarget(
                ghostTile,
                playerTile
        );
    }
}
