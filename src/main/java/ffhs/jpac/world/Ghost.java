package ffhs.jpac.world;

import ffhs.jpac.maze.MazePosition;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Bewegliche Gegnerfigur mit Freigabephase, Zustandslogik und Persönlichkeit.
 *
 * <p>Ein Geist wartet zunächst im Geisterhaus, folgt anschliessend einem
 * deterministischen Weg zum Ausgang und verwendet danach seine individuelle
 * Zielstrategie.</p>
 */
public class Ghost extends MovingEntity {

    /** Quadratische Kantenlänge eines Geistes in Pixeln. */
    public static final int SIZE = 18;
    private static final double SPEED = 100.0;
    private static final double ATTACK_RANGE = 20;

    private final Color color;
    private final GhostPersonality personality;
    private final GhostTargetStrategy targetStrategy;
    private final double releaseDelaySeconds;
    private final Random random = new Random();
    private GhostState currentState = new IdleState();
    private double releaseElapsedSeconds;
    private int lastDecisionRow = -1;
    private int lastDecisionCol = -1;
    private GhostReleaseState releaseState =
            GhostReleaseState.WAITING_IN_HOUSE;

    /**
     * Erstellt einen roten Geist ohne Freigabeverzögerung.
     *
     * @param x horizontale Spawnposition in Pixeln
     * @param y vertikale Spawnposition in Pixeln
     * @param color Darstellungsfarbe
     */
    public Ghost(double x, double y, Color color) {
        this(x, y, color, GhostPersonality.RED, 0);
    }

    /**
     * Erstellt einen konfigurierten Geist.
     *
     * @param x horizontale Spawnposition in Pixeln
     * @param y vertikale Spawnposition in Pixeln
     * @param color Darstellungsfarbe
     * @param personality Zielstrategie des Geistes
     * @param releaseDelaySeconds Wartezeit im Geisterhaus in Sekunden
     */
    public Ghost(
            double x,
            double y,
            Color color,
            GhostPersonality personality,
            double releaseDelaySeconds
    ) {
        super(x, y, SIZE, SPEED);
        this.color = color;
        this.personality = personality;
        this.targetStrategy = createTargetStrategy(personality);
        this.releaseDelaySeconds = releaseDelaySeconds;
    }

    /**
     * Gibt die Darstellungsfarbe zurück.
     *
     * @return Darstellungsfarbe des Geistes
     */
    public Color getColor() {
        return color;
    }

    /**
     * Gibt die konfigurierte Persönlichkeit zurück.
     *
     * @return Geistpersönlichkeit
     */
    public GhostPersonality getPersonality() {
        return personality;
    }

    /**
     * Gibt die Wartezeit vor dem Verlassen des Hauses zurück.
     *
     * @return Freigabeverzögerung in Sekunden
     */
    public double getReleaseDelay() {
        return releaseDelaySeconds;
    }

    /**
     * Prüft, ob die Freigabeverzögerung abgelaufen ist.
     *
     * @return {@code true}, sobald der Geist das Haus verlassen darf
     */
    public boolean isReleased() {
        return releaseElapsedSeconds >= releaseDelaySeconds;
    }

    /**
     * Prüft, ob der Geist das Geisterhaus verlassen hat.
     *
     * @return {@code true} im aktiven Zustand
     */
    public boolean hasLeftGhostHouse() {
        return releaseState == GhostReleaseState.ACTIVE;
    }

    /**
     * Prüft, ob die normale Geist-KI aktiv ist.
     *
     * @return {@code true} im Zustand {@link GhostReleaseState#ACTIVE}
     */
    public boolean isActive() {
        return releaseState == GhostReleaseState.ACTIVE;
    }

    /**
     * Gibt die aktuelle Phase der Hausfreigabe zurück.
     *
     * @return Freigabezustand
     */
    public GhostReleaseState getReleaseState() {
        return releaseState;
    }

    /**
     * Setzt Position, KI-Zustand, Timer und Strategie zurück.
     */
    @Override
    public void reset() {
        super.reset();
        currentState = new IdleState();
        releaseElapsedSeconds = 0;
        lastDecisionRow = -1;
        lastDecisionCol = -1;
        releaseState = GhostReleaseState.WAITING_IN_HOUSE;
        targetStrategy.reset();
    }

    /**
     * Stoppt die aktuelle Bewegung.
     */
    public void stopMoving() {
        dx = 0;
        dy = 0;
    }

    /**
     * Wählt im Leerlauf eine zulässige Richtung ohne unnötige Umkehr.
     *
     * @param world aktuelle Spielwelt
     * @return {@code true}, wenn eine Richtung gewählt wurde
     */
    protected boolean chooseIdleDirection(World world) {
        if (!canChooseDirection(world)) {
            return false;
        }

        List<Direction> directions = getAvailableDirections(world);
        if (directions.isEmpty()) {
            stopMoving();
            return false;
        }

        // Die Gegenrichtung bleibt ausgeschlossen, solange mindestens eine
        // Vorwärts- oder Seitenroute verfügbar ist.
        Direction oppositeDirection = getDirection().opposite();
        List<Direction> forwardAndSideDirections = new ArrayList<>(
                directions
        );
        forwardAndSideDirections.remove(oppositeDirection);

        Direction nextDirection;
        if (forwardAndSideDirections.isEmpty()) {
            nextDirection = oppositeDirection;
        } else {
            Collections.shuffle(forwardAndSideDirections, random);
            nextDirection = forwardAndSideDirections.getFirst();
        }

        snapToTileCenter(world);
        setDirection(nextDirection);
        rememberDecisionTile(world);
        return true;
    }

    /**
     * Wählt anhand der Persönlichkeitsstrategie den nächsten Weg zum Ziel.
     *
     * @param world aktuelle Spielwelt
     */
    protected void chasePlayer(World world) {
        if (!canChooseDirection(world)) {
            return;
        }

        Player player = world.getPlayer();
        if (player == null) {
            stopMoving();
            return;
        }

        TileMap map = world.getMap();
        int row = map.getTileRowFromPixel(y + size / 2.0);
        int col = map.getTileColFromPixel(x + size / 2.0);
        MazePosition target = getChaseTarget(world);
        Direction direction = map.getDirectionTowardTarget(
                row,
                col,
                target.row(),
                target.col(),
                getDirection()
        );

        if (direction == Direction.NONE) {
            stopMoving();
            return;
        }

        snapToTileCenter(world);
        setDirection(direction);
        rememberDecisionTile(world);
    }

    MazePosition getChaseTarget(World world) {
        return targetStrategy.getTarget(this, world);
    }

    MazePosition getPlayerTile(World world) {
        TileMap map = world.getMap();
        Player player = world.getPlayer();
        int playerRow = map.getTileRowFromPixel(
                player.getY() + player.getSize() / 2.0
        );
        int playerCol = map.getTileColFromPixel(
                player.getX() + player.getSize() / 2.0
        );
        return new MazePosition(playerRow, playerCol);
    }

    MazePosition getCurrentTile(World world) {
        TileMap map = world.getMap();
        return new MazePosition(
                map.getTileRowFromPixel(y + size / 2.0),
                map.getTileColFromPixel(x + size / 2.0)
        );
    }

    /**
     * Aktualisiert Freigabephase, Strategie und Bewegungszustand.
     *
     * @param world aktuelle Spielwelt
     * @param deltaTime vergangene Zeit seit dem letzten Frame in Sekunden
     */
    @Override
    public void update(World world, double deltaTime) {
        if (releaseState == GhostReleaseState.WAITING_IN_HOUSE) {
            releaseElapsedSeconds += deltaTime;
            if (!isReleased()) {
                stopMoving();
                return;
            }

            releaseState = isInsideGhostHouse(world)
                    ? GhostReleaseState.LEAVING_HOUSE
                    : GhostReleaseState.ACTIVE;
        }

        // Während des Verlassens darf keine zufällige oder verfolgende
        // Strategie die kürzeste Route zum Ausgang überschreiben.
        if (releaseState == GhostReleaseState.LEAVING_HOUSE) {
            leaveGhostHouse(world, deltaTime);
            return;
        }

        targetStrategy.update(deltaTime);
        updateState(world);
        currentState.update(this, world, deltaTime);
    }

    private boolean isInsideGhostHouse(World world) {
        int row = world.getMap().getTileRowFromPixel(y + size / 2.0);
        int col = world.getMap().getTileColFromPixel(x + size / 2.0);
        return world.getMap().isInside(row, col)
                && world.getMap().isGhostHouse(row, col);
    }

    private void updateState(World world) {
        Player player = world.getPlayer();
        if (player == null) {
            currentState = new IdleState();
            return;
        }

        double distance = Math.hypot(player.getX() - x, player.getY() - y);
        if (distance < ATTACK_RANGE) {
            currentState = new AttackState();
        } else {
            currentState = new ChaseState();
        }
    }

    private GhostTargetStrategy createTargetStrategy(
            GhostPersonality personality
    ) {
        return switch (personality) {
            case RED -> new RedTargetStrategy();
            case PINK -> new PinkTargetStrategy();
            case BLUE -> new BlueTargetStrategy();
            case ORANGE -> new OrangeTargetStrategy();
        };
    }

    private boolean canChooseDirection(World world) {
        if (dx == 0 && dy == 0) {
            return true;
        }

        if (isCenteredOnTile(world, 2) && isOnNewDecisionTile(world)) {
            return true;
        }

        return false;
    }

    private List<Direction> getAvailableDirections(World world) {
        List<Direction> directions = new ArrayList<>();

        for (Direction direction : List.of(
                Direction.UP,
                Direction.DOWN,
                Direction.LEFT,
                Direction.RIGHT
        )) {
            if (canReachNextTile(world, direction)) {
                directions.add(direction);
            }
        }

        return directions;
    }

    Direction getDirection() {
        if (dx < 0) {
            return Direction.LEFT;
        }
        if (dx > 0) {
            return Direction.RIGHT;
        }
        if (dy < 0) {
            return Direction.UP;
        }
        if (dy > 0) {
            return Direction.DOWN;
        }
        return Direction.NONE;
    }

    void setDirection(Direction direction) {
        dx = direction.getDx();
        dy = direction.getDy();
    }

    private boolean canReachNextTile(World world, Direction direction) {
        return world.canMove(
                this,
                direction,
                world.getMap().getTileSize()
        );
    }

    private boolean isOnNewDecisionTile(World world) {
        int tileSize = world.getMap().getTileSize();
        int row = (int) ((y + size / 2.0) / tileSize);
        int col = (int) ((x + size / 2.0) / tileSize);
        return row != lastDecisionRow || col != lastDecisionCol;
    }

    private void rememberDecisionTile(World world) {
        int tileSize = world.getMap().getTileSize();
        lastDecisionRow = (int) ((y + size / 2.0) / tileSize);
        lastDecisionCol = (int) ((x + size / 2.0) / tileSize);
    }

    private void leaveGhostHouse(World world, double deltaTime) {
        int row = world.getMap().getTileRowFromPixel(y + size / 2.0);
        int col = world.getMap().getTileColFromPixel(x + size / 2.0);

        if (world.getMap().isGhostHouseExit(row, col)
                && isCenteredOnTile(world, 2)) {
            snapToTileCenter(world);
            releaseState = GhostReleaseState.ACTIVE;
            stopMoving();
            return;
        }

        // Richtungsentscheidungen erfolgen nur an Kachelmittelpunkten, damit
        // der Geist innerhalb enger Hausgänge nicht hin- und herzittert.
        if (canChooseDirection(world)) {
            Direction exitDirection =
                    world.getMap().getDirectionTowardNearestGhostHouseExit(
                            row,
                            col,
                            getDirection()
                    );
            if (exitDirection == Direction.NONE) {
                stopMoving();
                return;
            }

            snapToTileCenter(world);
            setDirection(exitDirection);
            rememberDecisionTile(world);
        }

        move(world, deltaTime);
    }
}
