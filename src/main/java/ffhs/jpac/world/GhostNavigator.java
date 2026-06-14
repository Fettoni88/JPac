package ffhs.jpac.world;

import ffhs.jpac.maze.MazePosition;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

final class GhostNavigator {

    private static final List<Direction> DIRECTIONS = List.of(
            Direction.UP,
            Direction.DOWN,
            Direction.LEFT,
            Direction.RIGHT
    );

    private final TileMap map;

    GhostNavigator(TileMap map) {
        this.map = map;
    }

    Direction directionToNearestHouseExit(
            int startRow,
            int startCol,
            Direction currentDirection
    ) {
        if (map.isGhostHouseExit(startRow, startCol)) {
            return Direction.NONE;
        }

        int[][] distances = createHouseExitDistances();
        List<Direction> bestDirections = directionsWithShortestDistance(
                startRow,
                startCol,
                distances,
                true
        );
        return preferCurrentDirection(bestDirections, currentDirection);
    }

    boolean hasActivePath(
            int startRow,
            int startCol,
            int targetRow,
            int targetCol
    ) {
        int[][] distances = createActiveDistances(targetRow, targetCol);
        return map.isInside(startRow, startCol)
                && distances[startRow][startCol] >= 0;
    }

    Direction directionToTarget(
            int startRow,
            int startCol,
            int targetRow,
            int targetCol,
            Direction currentDirection
    ) {
        if (startRow == targetRow && startCol == targetCol) {
            return Direction.NONE;
        }

        int[][] distances = createActiveDistances(targetRow, targetCol);
        List<Direction> validDirections = reachableDirections(
                startRow,
                startCol,
                distances
        );
        return avoidReverse(validDirections, currentDirection, distances,
                startRow, startCol);
    }

    private List<Direction> reachableDirections(
            int startRow,
            int startCol,
            int[][] distances
    ) {
        List<Direction> directions = new ArrayList<>();

        for (Direction direction : DIRECTIONS) {
            int row = startRow + direction.getDy();
            int col = startCol + direction.getDx();
            if (map.isInside(row, col) && distances[row][col] >= 0) {
                directions.add(direction);
            }
        }

        return directions;
    }

    private List<Direction> directionsWithShortestDistance(
            int startRow,
            int startCol,
            int[][] distances,
            boolean houseOnly
    ) {
        int shortestDistance = Integer.MAX_VALUE;
        List<Direction> bestDirections = new ArrayList<>();

        for (Direction direction : DIRECTIONS) {
            int row = startRow + direction.getDy();
            int col = startCol + direction.getDx();
            boolean walkable = houseOnly
                    ? isHouseReleaseTile(row, col)
                    : map.isInside(row, col);

            if (!walkable || distances[row][col] < 0) {
                continue;
            }

            int distance = distances[row][col];
            if (distance < shortestDistance) {
                shortestDistance = distance;
                bestDirections.clear();
            }
            if (distance == shortestDistance) {
                bestDirections.add(direction);
            }
        }

        return bestDirections;
    }

    private Direction preferCurrentDirection(
            List<Direction> directions,
            Direction currentDirection
    ) {
        if (directions.isEmpty()) {
            return Direction.NONE;
        }
        if (directions.contains(currentDirection)) {
            return currentDirection;
        }

        Direction opposite = currentDirection.opposite();
        return directions.stream()
                .filter(direction -> direction != opposite)
                .findFirst()
                .orElse(directions.getFirst());
    }

    private Direction avoidReverse(
            List<Direction> validDirections,
            Direction currentDirection,
            int[][] distances,
            int startRow,
            int startCol
    ) {
        if (validDirections.isEmpty()) {
            return Direction.NONE;
        }

        Direction opposite = currentDirection.opposite();
        List<Direction> candidates = validDirections.stream()
                .filter(direction -> direction != opposite)
                .toList();
        if (candidates.isEmpty()) {
            candidates = validDirections;
        }

        Direction bestDirection = candidates.getFirst();
        int bestDistance = Integer.MAX_VALUE;
        for (Direction direction : candidates) {
            int row = startRow + direction.getDy();
            int col = startCol + direction.getDx();
            int distance = distances[row][col];
            if (distance < bestDistance
                    || distance == bestDistance
                    && direction == currentDirection) {
                bestDistance = distance;
                bestDirection = direction;
            }
        }
        return bestDirection;
    }

    private int[][] createHouseExitDistances() {
        int[][] distances = createEmptyDistances();
        Queue<MazePosition> openTiles = new ArrayDeque<>();

        for (MazePosition exit : effectiveHouseExits()) {
            distances[exit.row()][exit.col()] = 0;
            openTiles.add(exit);
        }

        fillDistances(openTiles, distances, true);
        return distances;
    }

    private int[][] createActiveDistances(int targetRow, int targetCol) {
        int[][] distances = createEmptyDistances();
        if (!map.isWalkableForActiveGhost(targetRow, targetCol)) {
            return distances;
        }

        Queue<MazePosition> openTiles = new ArrayDeque<>();
        distances[targetRow][targetCol] = 0;
        openTiles.add(new MazePosition(targetRow, targetCol));
        fillDistances(openTiles, distances, false);
        return distances;
    }

    private void fillDistances(
            Queue<MazePosition> openTiles,
            int[][] distances,
            boolean houseOnly
    ) {
        while (!openTiles.isEmpty()) {
            MazePosition current = openTiles.remove();

            for (Direction direction : DIRECTIONS) {
                int row = current.row() + direction.getDy();
                int col = current.col() + direction.getDx();
                boolean walkable = houseOnly
                        ? isHouseReleaseTile(row, col)
                        : map.isWalkableForActiveGhost(row, col);

                if (!walkable || distances[row][col] >= 0) {
                    continue;
                }

                distances[row][col] =
                        distances[current.row()][current.col()] + 1;
                openTiles.add(new MazePosition(row, col));
            }
        }
    }

    private int[][] createEmptyDistances() {
        int[][] distances = new int[map.getRows()][map.getCols()];
        for (int[] row : distances) {
            Arrays.fill(row, -1);
        }
        return distances;
    }

    private List<MazePosition> effectiveHouseExits() {
        List<MazePosition> exits = map.getGhostHouseExits();
        if (!exits.isEmpty()) {
            return exits;
        }

        int[] exit = map.findGhostHouseExit();
        return List.of(new MazePosition(exit[2], exit[3]));
    }

    private boolean isHouseReleaseTile(int row, int col) {
        return map.isInside(row, col)
                && (map.isGhostHouse(row, col)
                || map.isGhostHouseExit(row, col));
    }
}
