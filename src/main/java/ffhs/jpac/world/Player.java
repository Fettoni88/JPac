package ffhs.jpac.world;

/**
 * Repräsentiert die vom Benutzer gesteuerte Spielfigur.
 *
 * <p>Die Bewegung erfolgt kontinuierlich zwischen Kachelmittelpunkten.
 * Richtungswünsche werden kurz gepuffert und nur an zulässigen Kreuzungen
 * übernommen.</p>
 */
public class Player extends MovingEntity {

    /** Quadratische Kantenlänge des Spielers in Pixeln. */
    public static final int SIZE = 18;
    private static final double CENTER_TOLERANCE = 2.0;
    private static final double POSITION_EPSILON = 0.0001;
    private static final double INPUT_BUFFER_DURATION = 0.25;

    private Direction currentDirection = Direction.NONE;
    private Direction desiredDirection = Direction.NONE;
    private double inputBufferTimer;

    /**
     * Erstellt einen Spieler an der angegebenen Spawnposition.
     *
     * @param x horizontale Spawnposition in Pixeln
     * @param y vertikale Spawnposition in Pixeln
     */
    public Player(double x, double y) {
        super(x, y, SIZE, 200.0);
    }

    /**
     * Setzt den horizontalen Bewegungsanteil.
     *
     * @param dx horizontaler Richtungsanteil
     */
    public void setDx(int dx) {
        this.dx = dx;
    }

    /**
     * Setzt den vertikalen Bewegungsanteil.
     *
     * @param dy vertikaler Richtungsanteil
     */
    public void setDy(int dy) {
        this.dy = dy;
    }

    /**
     * Puffert einen Richtungswunsch für eine kurze Zeitspanne.
     *
     * @param direction gewünschte Bewegungsrichtung
     */
    public void setDesiredDirection(Direction direction) {
        desiredDirection = direction;
        inputBufferTimer = direction == Direction.NONE
                ? 0
                : INPUT_BUFFER_DURATION;
    }

    /**
     * Gibt die aktuell ausgeführte Bewegungsrichtung zurück.
     *
     * @return aktuelle Richtung
     */
    public Direction getCurrentDirection() {
        return currentDirection;
    }

    /**
     * Setzt Position, Bewegungsrichtung und Eingabepuffer zurück.
     */
    @Override
    public void reset() {
        super.reset();
        currentDirection = Direction.NONE;
        desiredDirection = Direction.NONE;
        inputBufferTimer = 0;
    }

    /**
     * Bewegt den Spieler kachelzentriert durch das Labyrinth.
     *
     * @param world aktuelle Spielwelt
     * @param deltaTime vergangene Zeit seit dem letzten Frame in Sekunden
     */
    @Override
    public void update(World world, double deltaTime) {
        double remainingTime = Math.max(0, deltaTime);

        // Ein grosser Frame kann mehrere Kachelmittelpunkte überschreiten.
        // Teilbewegungen stellen sicher, dass an jedem Mittelpunkt eine
        // Richtungs- und Kollisionsentscheidung erfolgt.
        while (remainingTime > POSITION_EPSILON) {
            if (isNearTileCenter(world)) {
                snapPlayerToTileCenter(world);
                chooseDirectionAtTileCenter(world);
            }

            if (currentDirection == Direction.NONE) {
                stop();
                updateInputBuffer(remainingTime);
                return;
            }

            double distanceToNextCenter = distanceToNextTileCenter(
                    world,
                    currentDirection
            );
            if (distanceToNextCenter <= POSITION_EPSILON) {
                snapPlayerToTileCenter(world);
                continue;
            }

            double stepTime = Math.min(
                    remainingTime,
                    distanceToNextCenter / speed
            );
            double step = speed * stepTime;
            moveTowardNextCenter(world, currentDirection, step);
            remainingTime -= stepTime;
            updateInputBuffer(stepTime);

            if (step + POSITION_EPSILON >= distanceToNextCenter) {
                snapPlayerToTileCenter(world);
            }
        }

        dx = currentDirection.getDx();
        dy = currentDirection.getDy();
    }

    /**
     * Begrenzt die horizontale Position auf ein Intervall.
     *
     * @param min kleinster zulässiger Wert
     * @param max grösster zulässiger Wert
     */
    public void clampX(double min, double max) {
        if (x < min) x = min;
        if (x > max) x = max;
    }

    /**
     * Begrenzt die vertikale Position auf ein Intervall.
     *
     * @param min kleinster zulässiger Wert
     * @param max grösster zulässiger Wert
     */
    public void clampY(double min, double max) {
        if (y < min) y = min;
        if (y > max) y = max;
    }

    private void chooseDirectionAtTileCenter(World world) {
        if (hasBufferedDirection()
                && canMoveToNextTile(world, desiredDirection)) {
            currentDirection = desiredDirection;
            clearInputBuffer();
        } else if (!canMoveToNextTile(world, currentDirection)) {
            currentDirection = Direction.NONE;
        }

        dx = currentDirection.getDx();
        dy = currentDirection.getDy();
    }

    private boolean hasBufferedDirection() {
        return desiredDirection != Direction.NONE
                && inputBufferTimer > POSITION_EPSILON;
    }

    private void updateInputBuffer(double elapsedTime) {
        if (!hasBufferedDirection()) {
            return;
        }

        inputBufferTimer = Math.max(
                0,
                inputBufferTimer - elapsedTime
        );
        if (inputBufferTimer <= POSITION_EPSILON) {
            clearInputBuffer();
        }
    }

    private void clearInputBuffer() {
        desiredDirection = Direction.NONE;
        inputBufferTimer = 0;
    }

    private boolean canMoveToNextTile(
            World world,
            Direction direction
    ) {
        if (direction == Direction.NONE) {
            return false;
        }

        TileMap map = world.getMap();
        int row = map.getTileRowFromPixel(getCenterY());
        int col = map.getTileColFromPixel(getCenterX());
        int targetRow = row + direction.getDy();
        int targetCol = col + direction.getDx();
        return map.isWalkableForPlayer(targetRow, targetCol);
    }

    private boolean isNearTileCenter(World world) {
        TileMap map = world.getMap();
        int row = nearestTileRow(map);
        int col = nearestTileCol(map);
        return Math.abs(getCenterX() - map.getTileCenterX(col))
                <= CENTER_TOLERANCE
                && Math.abs(getCenterY() - map.getTileCenterY(row))
                <= CENTER_TOLERANCE;
    }

    private void snapPlayerToTileCenter(World world) {
        TileMap map = world.getMap();
        int row = nearestTileRow(map);
        int col = nearestTileCol(map);
        x = map.getTileCenterX(col) - size / 2.0;
        y = map.getTileCenterY(row) - size / 2.0;
    }

    private double distanceToNextTileCenter(
            World world,
            Direction direction
    ) {
        TileMap map = world.getMap();
        if (direction.getDx() != 0) {
            int targetCol = nextTileIndex(
                    getCenterX(),
                    map.getTileSize(),
                    direction.getDx()
            );
            return Math.abs(
                    map.getTileCenterX(targetCol) - getCenterX()
            );
        }

        int targetRow = nextTileIndex(
                getCenterY(),
                map.getTileSize(),
                direction.getDy()
        );
        return Math.abs(
                map.getTileCenterY(targetRow) - getCenterY()
        );
    }

    private int nextTileIndex(
            double centerPosition,
            int tileSize,
            int direction
    ) {
        double gridPosition =
                (centerPosition - tileSize / 2.0) / tileSize;
        if (direction > 0) {
            return (int) Math.floor(gridPosition + POSITION_EPSILON) + 1;
        }
        return (int) Math.ceil(gridPosition - POSITION_EPSILON) - 1;
    }

    private void moveTowardNextCenter(
            World world,
            Direction direction,
            double distance
    ) {
        TileMap map = world.getMap();
        int row = nearestTileRow(map);
        int col = nearestTileCol(map);

        if (direction.getDx() != 0) {
            y = map.getTileCenterY(row) - size / 2.0;
            x += direction.getDx() * distance;
        } else {
            x = map.getTileCenterX(col) - size / 2.0;
            y += direction.getDy() * distance;
        }
    }

    private int nearestTileRow(TileMap map) {
        return (int) Math.round(
                (getCenterY() - map.getTileSize() / 2.0)
                        / map.getTileSize()
        );
    }

    private int nearestTileCol(TileMap map) {
        return (int) Math.round(
                (getCenterX() - map.getTileSize() / 2.0)
                        / map.getTileSize()
        );
    }

    private double getCenterX() {
        return x + size / 2.0;
    }

    private double getCenterY() {
        return y + size / 2.0;
    }

    private void stop() {
        dx = 0;
        dy = 0;
    }
}
