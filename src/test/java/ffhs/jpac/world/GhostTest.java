package ffhs.jpac.world;

import ffhs.jpac.maze.MazePosition;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GhostTest {

    private World createWorld(Player player) {
        TileMap map = new TileMap("/maps/map.txt");
        World world = new World(
                map.getCols() * map.getTileSize(),
                map.getRows() * map.getTileSize(),
                map
        );
        world.setPlayer(player);
        return world;
    }

    @Test
    void delayedGhostWaitsBeforeMoving() {
        TileMap map = new TileMap("/mazes/maze1.json");
        World world = createWorldWithMap(map);
        Ghost pink = createGhostAtSpawn(
                world,
                map.getGhostSpawns().get(1),
                5
        );
        double spawnX = pink.getX();
        double spawnY = pink.getY();

        pink.update(world, 4.9);
        assertEquals(
                GhostReleaseState.WAITING_IN_HOUSE,
                pink.getReleaseState()
        );
        assertEquals(spawnX, pink.getX(), 0.0001);
        assertEquals(spawnY, pink.getY(), 0.0001);

        pink.update(world, 0.1);
        assertEquals(
                GhostReleaseState.LEAVING_HOUSE,
                pink.getReleaseState()
        );
        assertTrue(pink.isReleased());
    }

    @Test
    void restartResetsReleaseDelay() {
        Player player = new Player(243, 315);
        World world = createWorld(player);
        Ghost cyan = new Ghost(
                267, 195, Color.CYAN, GhostPersonality.BLUE, 10
        );

        cyan.update(world, 10);
        assertTrue(cyan.isReleased());

        cyan.reset();

        assertFalse(cyan.isReleased());
        assertEquals(267, cyan.getX(), 0.0001);
        assertEquals(195, cyan.getY(), 0.0001);
    }

    @Test
    void ghostMovementStaysOnOneGridLane() {
        Player player = new Player(243, 315);
        World world = createWorld(player);
        Ghost red = new Ghost(
                219, 195, Color.RED, GhostPersonality.RED, 0
        );

        double oldX = red.getX();
        double oldY = red.getY();
        red.update(world, 0.05);

        boolean movedHorizontally = red.getX() != oldX && red.getY() == oldY;
        boolean movedVertically = red.getY() != oldY && red.getX() == oldX;
        assertTrue(movedHorizontally || movedVertically);
    }

    @Test
    void releasedGhostKeepsMovingBeyondSpawnTile() {
        Player player = new Player(243, 315);
        World world = createWorld(player);
        Ghost red = new Ghost(
                219, 195, Color.RED, GhostPersonality.RED, 0
        );

        for (int frame = 0; frame < 120; frame++) {
            red.update(world, 1.0 / 60.0);
        }

        double distanceFromSpawn = Math.hypot(
                red.getX() - 219,
                red.getY() - 195
        );
        assertTrue(distanceFromSpawn > 20);
    }

    @Test
    void redGhostLeavesGhostHouseInsteadOfPushingIntoWall() {
        Player player = new Player(243, 315);
        World world = createWorld(player);
        Ghost red = new Ghost(
                219, 195, Color.RED, GhostPersonality.RED, 0
        );

        for (int frame = 0; frame < 120; frame++) {
            red.update(world, 1.0 / 60.0);
        }

        assertTrue(red.hasLeftGhostHouse());
        assertTrue(red.getY() >= 264);
    }

    @Test
    void ghostsKeepTheirConfiguredPersonalities() {
        Ghost orange = new Ghost(
                243, 219, Color.ORANGE, GhostPersonality.ORANGE, 15
        );

        assertEquals(GhostPersonality.ORANGE, orange.getPersonality());
        assertEquals(Color.ORANGE, orange.getColor());
    }

    @Test
    void allGhostsUseTheirDelayThenLeaveThroughExit() {
        Player player = new Player(27, 27);
        World world = createWorld(player);
        Ghost[] ghosts = {
                new Ghost(219, 195, Color.RED, GhostPersonality.RED, 0),
                new Ghost(243, 195, Color.PINK, GhostPersonality.PINK, 5),
                new Ghost(267, 195, Color.CYAN, GhostPersonality.BLUE, 10),
                new Ghost(243, 219, Color.ORANGE, GhostPersonality.ORANGE, 15)
        };

        for (int second = 0; second < 18; second++) {
            for (int frame = 0; frame < 60; frame++) {
                for (Ghost ghost : ghosts) {
                    ghost.update(world, 1.0 / 60.0);
                }
            }
        }

        for (Ghost ghost : ghosts) {
            assertTrue(ghost.isReleased());
            assertTrue(ghost.hasLeftGhostHouse());
        }
    }

    @Test
    void firstGhostReleasesImmediatelyAndLaterGhostUsesDelay() {
        TileMap map = new TileMap("/mazes/maze1.json");
        World world = createWorldWithMap(map);
        Ghost red = createGhostAtSpawn(
                world,
                map.getGhostSpawns().getFirst(),
                0
        );
        Ghost pink = createGhostAtSpawn(
                world,
                map.getGhostSpawns().get(1),
                5
        );

        assertTrue(red.isReleased());
        assertFalse(pink.isReleased());
        assertEquals(
                GhostReleaseState.WAITING_IN_HOUSE,
                red.getReleaseState()
        );

        red.update(world, 0);
        assertEquals(
                GhostReleaseState.LEAVING_HOUSE,
                red.getReleaseState()
        );
        pink.update(world, 4.9);
        assertFalse(pink.isReleased());

        pink.update(world, 0.1);
        assertTrue(pink.isReleased());
    }

    @Test
    void idleGhostDoesNotReverseWhenAnotherDirectionIsAvailable() {
        TileMap map = new TileMap("/mazes/maze1.json");
        World world = createWorldWithMap(map);
        Ghost ghost = findGhostAtJunction(world, Direction.RIGHT);

        ghost.setDirection(Direction.RIGHT);
        assertTrue(ghost.chooseIdleDirection(world));

        assertNotEquals(Direction.LEFT, ghost.getDirection());
    }

    @Test
    void idleGhostReversesAtDeadEnd() {
        TileMap map = new TileMap("/mazes/maze1.json");
        World world = createWorldWithMap(map);
        Ghost ghost = findGhostAtDeadEnd(world);
        Direction onlyExit = findAvailableDirections(world, ghost).getFirst();

        ghost.setDirection(onlyExit.opposite());
        assertTrue(ghost.chooseIdleDirection(world));

        assertEquals(onlyExit, ghost.getDirection());
    }

    @Test
    void pinkTargetsFourTilesAheadOfPlayer() {
        TileMap map = new TileMap("/mazes/maze1.json");
        World world = createWorldWithMap(map);
        MazePosition playerTile = findStraightPlayerTile(map, true);
        Player player = createCenteredPlayer(world, playerTile);
        player.setDesiredDirection(Direction.RIGHT);
        player.update(world, 0.001);
        world.setPlayer(player);
        Ghost pink = createActiveGhostNear(
                world,
                playerTile,
                GhostPersonality.PINK
        );

        MazePosition target = pink.getChaseTarget(world);

        assertEquals(playerTile.row(), target.row());
        assertEquals(playerTile.col() + 4, target.col());
    }

    @Test
    void pinkFallsBackToPlayerWhenAheadTileIsBlocked() {
        TileMap map = new TileMap("/mazes/maze1.json");
        World world = createWorldWithMap(map);
        MazePosition playerTile = findStraightPlayerTile(map, false);
        Player player = createCenteredPlayer(world, playerTile);
        player.setDesiredDirection(Direction.RIGHT);
        player.update(world, 0.001);
        world.setPlayer(player);
        Ghost pink = createActiveGhostNear(
                world,
                playerTile,
                GhostPersonality.PINK
        );

        MazePosition target = pink.getChaseTarget(world);

        assertEquals(playerTile, target);
    }

    @Test
    void activeChasingGhostAvoidsImmediateReverse() {
        TileMap map = new TileMap("/mazes/maze1.json");
        World world = createWorldWithMap(map);
        Ghost red = findGhostAtJunction(world, Direction.RIGHT);
        Player player = new Player(red.getX(), red.getY());
        world.setPlayer(player);
        red.setDirection(Direction.RIGHT);

        assertTrue(red.getReleaseState()
                == GhostReleaseState.WAITING_IN_HOUSE);
        red.update(world, 0);
        red.chasePlayer(world);

        assertNotEquals(Direction.LEFT, red.getDirection());
    }

    @Test
    void redAlwaysTargetsPlayerTile() {
        TileMap map = new TileMap("/mazes/maze1.json");
        World world = createWorldWithMap(map);
        MazePosition playerTile = map.getPlayerSpawn();
        world.setPlayer(createCenteredPlayer(world, playerTile));
        Ghost red = createActiveGhostNear(
                world,
                playerTile,
                GhostPersonality.RED
        );

        assertEquals(playerTile, red.getChaseTarget(world));
    }

    @Test
    void blueReturnsToPatrolAfterTemporaryChase() {
        TileMap map = new TileMap("/mazes/maze1.json");
        World world = createWorldWithMap(map);
        MazePosition playerTile = map.getPlayerSpawn();
        world.setPlayer(createCenteredPlayer(world, playerTile));
        Ghost blue = createActiveGhostNear(
                world,
                playerTile,
                GhostPersonality.BLUE
        );
        BlueTargetStrategy strategy = new BlueTargetStrategy();

        MazePosition chaseTarget = strategy.getTarget(blue, world);
        strategy.update(4.1);
        MazePosition patrolTarget = strategy.getTarget(blue, world);

        assertEquals(playerTile, chaseTarget);
        assertNotEquals(playerTile, patrolTarget);
        assertTrue(map.getPatrolTargets().contains(patrolTarget));
    }

    @Test
    void orangeChasesFarPlayerAndFleesClosePlayer() {
        TileMap map = new TileMap("/mazes/maze1.json");
        World world = createWorldWithMap(map);
        OrangeTargetStrategy strategy = new OrangeTargetStrategy();
        MazePosition farPlayerTile = map.getPatrolTargets().getLast();
        world.setPlayer(createCenteredPlayer(world, farPlayerTile));
        Ghost orange = createGhostAtTile(
                world,
                map.getPatrolTargets().getFirst(),
                GhostPersonality.ORANGE
        );

        assertEquals(
                farPlayerTile,
                strategy.getTarget(orange, world)
        );

        MazePosition closePlayerTile = findNeighborTile(
                map,
                orange.getCurrentTile(world)
        );
        world.setPlayer(createCenteredPlayer(world, closePlayerTile));
        MazePosition fleeTarget = strategy.getTarget(orange, world);

        assertNotEquals(closePlayerTile, fleeTarget);
        assertTrue(
                tileDistance(fleeTarget, closePlayerTile)
                        > tileDistance(
                                orange.getCurrentTile(world),
                                closePlayerTile
                        )
        );
    }

    @Test
    void leavingGhostChoosesDirectionTowardExit() {
        TileMap map = new TileMap("/mazes/maze1.json");
        World world = createWorldWithMap(map);
        MazePosition spawn = map.getGhostSpawns().getLast();
        Ghost red = createGhostAtSpawn(world, spawn, 0);
        int[] exit = map.findGhostHouseExit(
                red.getX() + red.getSize() / 2.0,
                red.getY() + red.getSize() / 2.0
        );
        int startDistance = Math.abs(spawn.row() - exit[2])
                + Math.abs(spawn.col() - exit[3]);

        red.update(world, 0);
        Direction direction = red.getDirection();
        int nextRow = spawn.row() + direction.getDy();
        int nextCol = spawn.col() + direction.getDx();
        int nextDistance = Math.abs(nextRow - exit[2])
                + Math.abs(nextCol - exit[3]);

        assertEquals(
                GhostReleaseState.LEAVING_HOUSE,
                red.getReleaseState()
        );
        assertTrue(nextDistance < startDistance);
    }

    @Test
    void ghostsLeaveTheHouseInAllJsonMazes() {
        for (int mazeNumber = 1; mazeNumber <= 5; mazeNumber++) {
            TileMap map = new TileMap(
                    "/mazes/maze" + mazeNumber + ".json"
            );
            MazePosition playerSpawn = map.getPlayerSpawn();
            int tileSize = map.getTileSize();
            Player player = new Player(
                    playerSpawn.col() * tileSize
                            + (tileSize - Player.SIZE) / 2.0,
                    playerSpawn.row() * tileSize
                            + (tileSize - Player.SIZE) / 2.0
            );
            World world = new World(
                    map.getCols() * tileSize,
                    map.getRows() * tileSize,
                    map
            );
            world.setPlayer(player);
            for (MazePosition ghostSpawn : map.getGhostSpawns()) {
                Ghost red = new Ghost(
                        ghostSpawn.col() * tileSize
                                + (tileSize - Ghost.SIZE) / 2.0,
                        ghostSpawn.row() * tileSize
                                + (tileSize - Ghost.SIZE) / 2.0,
                        Color.RED,
                        GhostPersonality.RED,
                        0
                );

                for (int frame = 0;
                     frame < 180 && !red.hasLeftGhostHouse();
                     frame++) {
                    red.update(world, 1.0 / 60.0);
                }

                assertTrue(red.isReleased());
                assertTrue(
                        red.hasLeftGhostHouse(),
                        "Ghost at " + ghostSpawn
                                + " did not leave maze " + mazeNumber
                );
            }
        }
    }

    private World createWorldWithMap(TileMap map) {
        return new World(
                map.getCols() * map.getTileSize(),
                map.getRows() * map.getTileSize(),
                map
        );
    }

    private Ghost findGhostAtJunction(
            World world,
            Direction currentDirection
    ) {
        for (int row = 1; row < world.getMap().getRows() - 1; row++) {
            for (int col = 1; col < world.getMap().getCols() - 1; col++) {
                Ghost ghost = createCenteredGhost(world, row, col);
                List<Direction> directions =
                        findAvailableDirections(world, ghost);

                if (directions.contains(currentDirection)
                        && directions.size() >= 3) {
                    return ghost;
                }
            }
        }

        throw new IllegalStateException("Maze has no suitable junction");
    }

    private Ghost findGhostAtDeadEnd(World world) {
        for (int row = 1; row < world.getMap().getRows() - 1; row++) {
            for (int col = 1; col < world.getMap().getCols() - 1; col++) {
                Ghost ghost = createCenteredGhost(world, row, col);
                if (findAvailableDirections(world, ghost).size() == 1) {
                    return ghost;
                }
            }
        }

        throw new IllegalStateException("Maze has no dead end");
    }

    private Ghost createCenteredGhost(World world, int row, int col) {
        int tileSize = world.getMap().getTileSize();
        return new Ghost(
                col * tileSize + (tileSize - Ghost.SIZE) / 2.0,
                row * tileSize + (tileSize - Ghost.SIZE) / 2.0,
                Color.CYAN,
                GhostPersonality.BLUE,
                0
        );
    }

    private Ghost createGhostAtSpawn(
            World world,
            MazePosition spawn,
            double releaseDelay
    ) {
        int tileSize = world.getMap().getTileSize();
        return new Ghost(
                spawn.col() * tileSize + (tileSize - Ghost.SIZE) / 2.0,
                spawn.row() * tileSize + (tileSize - Ghost.SIZE) / 2.0,
                Color.RED,
                GhostPersonality.RED,
                releaseDelay
        );
    }

    private Ghost createGhostAtTile(
            World world,
            MazePosition position,
            GhostPersonality personality
    ) {
        int tileSize = world.getMap().getTileSize();
        Ghost ghost = new Ghost(
                position.col() * tileSize
                        + (tileSize - Ghost.SIZE) / 2.0,
                position.row() * tileSize
                        + (tileSize - Ghost.SIZE) / 2.0,
                Color.ORANGE,
                personality,
                0
        );
        ghost.update(world, 0);
        return ghost;
    }

    private MazePosition findNeighborTile(
            TileMap map,
            MazePosition position
    ) {
        for (Direction direction : List.of(
                Direction.UP,
                Direction.DOWN,
                Direction.LEFT,
                Direction.RIGHT
        )) {
            int row = position.row() + direction.getDy();
            int col = position.col() + direction.getDx();
            if (map.isWalkableForPlayer(row, col)) {
                return new MazePosition(row, col);
            }
        }

        throw new IllegalStateException("Tile has no walkable neighbor");
    }

    private int tileDistance(
            MazePosition first,
            MazePosition second
    ) {
        return Math.abs(first.row() - second.row())
                + Math.abs(first.col() - second.col());
    }

    private MazePosition findStraightPlayerTile(
            TileMap map,
            boolean validFourTilesAhead
    ) {
        for (int row = 1; row < map.getRows() - 1; row++) {
            for (int col = 1; col < map.getCols() - 4; col++) {
                if (!map.isWalkableForPlayer(row, col)
                        || !map.isWalkableForPlayer(row, col + 1)) {
                    continue;
                }

                boolean targetIsValid =
                        map.isWalkableForPlayer(row, col + 4);
                if (targetIsValid == validFourTilesAhead) {
                    return new MazePosition(row, col);
                }
            }
        }

        throw new IllegalStateException("Maze has no suitable player tile");
    }

    private Player createCenteredPlayer(
            World world,
            MazePosition position
    ) {
        int tileSize = world.getMap().getTileSize();
        return new Player(
                position.col() * tileSize
                        + (tileSize - Player.SIZE) / 2.0,
                position.row() * tileSize
                        + (tileSize - Player.SIZE) / 2.0
        );
    }

    private Ghost createActiveGhostNear(
            World world,
            MazePosition playerTile,
            GhostPersonality personality
    ) {
        for (Direction direction : List.of(
                Direction.UP,
                Direction.DOWN,
                Direction.LEFT,
                Direction.RIGHT
        )) {
            int row = playerTile.row() + direction.getDy();
            int col = playerTile.col() + direction.getDx();
            if (!world.getMap().isWalkableForActiveGhost(row, col)) {
                continue;
            }

            int tileSize = world.getMap().getTileSize();
            Ghost ghost = new Ghost(
                    col * tileSize + (tileSize - Ghost.SIZE) / 2.0,
                    row * tileSize + (tileSize - Ghost.SIZE) / 2.0,
                    Color.PINK,
                    personality,
                    0
            );
            ghost.update(world, 0);
            return ghost;
        }

        throw new IllegalStateException("Player tile has no ghost neighbor");
    }

    private List<Direction> findAvailableDirections(
            World world,
            Ghost ghost
    ) {
        List<Direction> directions = new ArrayList<>();

        for (Direction direction : List.of(
                Direction.UP,
                Direction.DOWN,
                Direction.LEFT,
                Direction.RIGHT
        )) {
            if (world.canMove(
                    ghost,
                    direction,
                    world.getMap().getTileSize()
            )) {
                directions.add(direction);
            }
        }

        return directions;
    }
}
