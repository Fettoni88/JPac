package ffhs.jpac.world;

import ffhs.jpac.maze.MazePosition;

class PinkTargetStrategy implements GhostTargetStrategy {

    private static final int AMBUSH_DISTANCE = 4;

    @Override
    public MazePosition getTarget(
            Ghost ghost,
            World world
    ) {
        TileMap map = world.getMap();
        Player player = world.getPlayer();
        MazePosition playerTile = ghost.getPlayerTile(world);
        Direction direction = player.getCurrentDirection();

        if (direction == Direction.NONE) {
            return playerTile;
        }

        int targetRow = playerTile.row()
                + direction.getDy() * AMBUSH_DISTANCE;
        int targetCol = playerTile.col()
                + direction.getDx() * AMBUSH_DISTANCE;
        MazePosition ghostTile = ghost.getCurrentTile(world);

        if (map.isWalkableForActiveGhost(targetRow, targetCol)
                && map.hasActiveGhostPath(
                        ghostTile.row(),
                        ghostTile.col(),
                        targetRow,
                        targetCol
                )) {
            return new MazePosition(targetRow, targetCol);
        }

        return playerTile;
    }
}
