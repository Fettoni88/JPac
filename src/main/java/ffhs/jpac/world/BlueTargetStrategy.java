package ffhs.jpac.world;

import ffhs.jpac.maze.MazePosition;

import java.util.List;

class BlueTargetStrategy implements GhostTargetStrategy {

    private static final int CHASE_RANGE = 6;
    private static final double CHASE_DURATION = 4.0;
    private static final double PATROL_COOLDOWN = 3.0;

    private int patrolIndex;
    private double chaseTimer;
    private double patrolCooldown;

    @Override
    public void update(double deltaTime) {
        chaseTimer = Math.max(0, chaseTimer - deltaTime);
        patrolCooldown = Math.max(0, patrolCooldown - deltaTime);
    }

    @Override
    public MazePosition getTarget(Ghost ghost, World world) {
        MazePosition ghostTile = ghost.getCurrentTile(world);
        MazePosition playerTile = ghost.getPlayerTile(world);
        int playerDistance = tileDistance(ghostTile, playerTile);

        if (chaseTimer > 0) {
            return playerTile;
        }

        if (playerDistance <= CHASE_RANGE && patrolCooldown <= 0) {
            chaseTimer = CHASE_DURATION;
            patrolCooldown = CHASE_DURATION + PATROL_COOLDOWN;
            return playerTile;
        }

        List<MazePosition> patrolTargets =
                world.getMap().getPatrolTargets();
        MazePosition target = patrolTargets.get(patrolIndex);
        if (ghostTile.equals(target)) {
            patrolIndex = (patrolIndex + 1) % patrolTargets.size();
            target = patrolTargets.get(patrolIndex);
        }
        return target;
    }

    @Override
    public void reset() {
        patrolIndex = 0;
        chaseTimer = 0;
        patrolCooldown = 0;
    }

    private int tileDistance(
            MazePosition first,
            MazePosition second
    ) {
        return Math.abs(first.row() - second.row())
                + Math.abs(first.col() - second.col());
    }
}
