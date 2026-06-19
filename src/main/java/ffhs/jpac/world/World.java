package ffhs.jpac.world;

import ffhs.jpac.maze.MazePosition;
import ffhs.jpac.persistence.HighscoreEntry;
import ffhs.jpac.persistence.HighscoreManager;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Verwaltet die Spielwelt, ihren Zustand und die zentralen Spielregeln.
 *
 * <p>Dazu gehören Entitäten, Pellets, Kollisionen, Punkte, Spielende,
 * Neustart und die Anbindung an die Highscore-Persistenz.</p>
 */
public class World {

    private static final List<Color> GHOST_COLORS = List.of(
            Color.RED,
            Color.PINK,
            Color.CYAN,
            Color.ORANGE
    );
    private static final List<GhostPersonality> GHOST_PERSONALITIES = List.of(
            GhostPersonality.RED,
            GhostPersonality.PINK,
            GhostPersonality.BLUE,
            GhostPersonality.ORANGE
    );
    private static final List<Double> GHOST_RELEASE_DELAYS = List.of(
            0.0,
            5.0,
            10.0,
            15.0
    );

    private int width;
    private int height;
    private TileMap map;
    private final List<Entity> entities = new ArrayList<>();
    private final List<Pellet> pellets = new ArrayList<>();
    private final HighscoreManager highscoreManager;
    private String playerName;
    private List<HighscoreEntry> highscores;
    private Player player;
    private int score = 0;
    private GameState gameState = GameState.START_MENU;
    private boolean hasSavedHighscore;

    /**
     * Gibt die aktuelle Punktzahl zurück.
     *
     * @return aktuelle Punktzahl
     */
    public int getScore() {
        return score;
    }

    /**
     * Setzt die Spielerinstanz der Welt.
     *
     * @param player neue Spielerinstanz
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * Gibt die aktuelle Spielerinstanz zurück.
     *
     * @return Spielerinstanz oder {@code null}
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Erstellt eine Welt mit dem Standardspielernamen.
     *
     * @param width Weltbreite in Pixeln
     * @param height Welthöhe in Pixeln
     * @param map verwendete Karte
     */
    public World(int width, int height, TileMap map) {
        this(width, height, map, "Player");
    }

    /**
     * Erstellt eine Welt mit angegebenem Spielernamen.
     *
     * @param width Weltbreite in Pixeln
     * @param height Welthöhe in Pixeln
     * @param map verwendete Karte
     * @param playerName Spielername
     */
    public World(int width, int height, TileMap map, String playerName) {
        this(width, height, map, playerName, new HighscoreManager());
    }

    /**
     * Erstellt eine vollständig konfigurierte Spielwelt.
     *
     * @param width Weltbreite in Pixeln
     * @param height Welthöhe in Pixeln
     * @param map verwendete Karte
     * @param playerName Spielername
     * @param highscoreManager Persistenzdienst für Highscores
     */
    public World(
            int width,
            int height,
            TileMap map,
            String playerName,
            HighscoreManager highscoreManager
    ) {
        this.width = width;
        this.height = height;
        this.map = map;
        this.playerName = normalizePlayerName(playerName);
        this.highscoreManager = highscoreManager;
        this.highscores = highscoreManager.loadHighscores();
    }

    /**
     * Gibt die Weltbreite zurück.
     *
     * @return Weltbreite in Pixeln
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gibt die Welthöhe zurück.
     *
     * @return Welthöhe in Pixeln
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gibt die aktuell geladene Karte zurück.
     *
     * @return aktuelle Karte
     */
    public TileMap getMap() {
        return map;
    }

    /**
     * Gibt den normalisierten Spielernamen zurück.
     *
     * @return Spielername
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Gibt den besten gespeicherten Highscore zurück.
     *
     * @return bester Eintrag oder {@code null} bei leerer Liste
     */
    public HighscoreEntry getBestHighscore() {
        if (highscores.isEmpty()) {
            return null;
        }
        return highscores.getFirst();
    }

    /**
     * Gibt die aktuell geladenen Highscores zurück.
     *
     * @return unveränderliche Kopie der Highscore-Liste
     */
    public List<HighscoreEntry> getHighscores() {
        return List.copyOf(highscores);
    }

    /**
     * Fügt eine Entität zur Welt hinzu.
     *
     * @param entity hinzuzufügende Entität
     */
    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    /**
     * Gibt die Entitäten der Welt zurück.
     *
     * @return veränderliche Liste der Weltentitäten
     */
    public List<Entity> getEntities() {
        return entities;
    }

    /**
     * Fügt ein Pellet zur Welt hinzu.
     *
     * @param pellet hinzuzufügendes Pellet
     */
    public void addPellet(Pellet pellet) {
        pellets.add(pellet);
    }

    /**
     * Gibt die Pellets der Welt zurück.
     *
     * @return veränderliche Liste der Pellets
     */
    public List<Pellet> getPellets() {
        return pellets;
    }

    /**
     * Gibt den aktuellen Zustand des Spielablaufs zurück.
     *
     * @return aktueller Spielzustand
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Öffnet aus dem Hauptmenü die Namenseingabe.
     */
    public void showNameInput() {
        if (gameState == GameState.START_MENU) {
            gameState = GameState.NAME_INPUT;
        }
    }

    /**
     * Bestätigt den eingegebenen Namen und öffnet die Labyrinthauswahl.
     *
     * @param name eingegebener Spielername
     */
    public void confirmPlayerName(String name) {
        if (gameState != GameState.NAME_INPUT) {
            return;
        }

        playerName = normalizePlayerName(name);
        gameState = GameState.MAZE_SELECTION;
    }

    /**
     * Lädt das gewählte Labyrinth und startet die Spielrunde.
     *
     * @param mazeId Kennung des Labyrinths ohne Dateiendung
     */
    public void startMaze(String mazeId) {
        if (gameState != GameState.MAZE_SELECTION) {
            return;
        }

        TileMap selectedMap = new TileMap(
                "/mazes/" + mazeId + ".json"
        );
        initializeMaze(selectedMap);
        gameState = GameState.PLAYING;
    }

    /**
     * Lädt die Highscores neu und zeigt die Highscore-Ansicht.
     */
    public void showHighscores() {
        highscores = highscoreManager.loadHighscores();
        gameState = GameState.HIGHSCORE;
    }

    /**
     * Wechselt zum Hauptmenü.
     */
    public void showStartMenu() {
        gameState = GameState.START_MENU;
    }

    /**
     * Startet die Runde mit dem bereits gespeicherten Spielernamen.
     */
    public void startGame() {
        startGame(playerName);
    }

    /**
     * Startet die Runde mit einem neuen Spielernamen.
     *
     * @param name Spielername
     */
    public void startGame(String name) {
        playerName = normalizePlayerName(name);
        gameState = GameState.PLAYING;
    }

    /**
     * Lädt die aktuelle Runde vollständig neu und startet sofort.
     */
    public void restartGame() {
        resetCurrentSession();
        gameState = GameState.PLAYING;
    }

    /**
     * Setzt die aktuelle Sitzung zurück und kehrt zum Hauptmenü zurück.
     */
    public void returnToMainMenu() {
        resetCurrentSession();
        gameState = GameState.START_MENU;
    }

    private void resetCurrentSession() {
        String mazeId = map.getMazeId();
        if (mazeId != null && mazeId.startsWith("maze")) {
            initializeMaze(new TileMap("/mazes/" + mazeId + ".json"));
            return;
        }

        resetGame();
    }

    private void resetGame() {
        score = 0;
        hasSavedHighscore = false;

        for (Entity entity : entities) {
            entity.reset();
        }

        generatePellets();
    }

    private void initializeMaze(TileMap selectedMap) {
        map = selectedMap;
        width = map.getCols() * map.getTileSize();
        height = map.getRows() * map.getTileSize();
        score = 0;
        hasSavedHighscore = false;
        entities.clear();
        pellets.clear();

        player = createPlayer();
        entities.add(player);
        spawnGhosts();
        generatePellets();
    }

    private Player createPlayer() {
        MazePosition spawn = map.getPlayerSpawn();
        return new Player(
                spawnX(spawn, Player.SIZE),
                spawnY(spawn, Player.SIZE)
        );
    }

    private void spawnGhosts() {
        List<MazePosition> spawns = map.getGhostSpawns();
        for (int index = 0; index < GHOST_COLORS.size(); index++) {
            MazePosition spawn = spawns.get(index);
            entities.add(new Ghost(
                    spawnX(spawn, Ghost.SIZE),
                    spawnY(spawn, Ghost.SIZE),
                    GHOST_COLORS.get(index),
                    GHOST_PERSONALITIES.get(index),
                    GHOST_RELEASE_DELAYS.get(index)
            ));
        }
    }

    private double spawnX(MazePosition position, int entitySize) {
        return position.col() * map.getTileSize()
                + (map.getTileSize() - entitySize) / 2.0;
    }

    private double spawnY(MazePosition position, int entitySize) {
        return position.row() * map.getTileSize()
                + (map.getTileSize() - entitySize) / 2.0;
    }

    /**
     * Aktualisiert alle aktiven Spielobjekte und wertet Spielregeln aus.
     *
     * @param deltaTime vergangene Zeit seit dem letzten Frame in Sekunden
     */
    public void update(double deltaTime) {

        if (gameState != GameState.PLAYING) {
            return;
        }

        for (Entity entity : entities) {
            entity.update(this, deltaTime);
        }

        checkPelletCollection();
        checkGhostCollision();

        if (gameState == GameState.PLAYING && areAllPelletsCollected()) {
            endGame(GameState.WIN);
        }
    }

    private void endGame(GameState endState) {
        if (gameState != GameState.PLAYING) {
            return;
        }

        gameState = endState;
        saveHighscoreIfNeeded();
    }

    private void saveHighscoreIfNeeded() {
        // WIN und GAME_OVER können über mehrere Frames sichtbar bleiben.
        // Das Flag verhindert dadurch doppelte Einträge derselben Runde.
        if (hasSavedHighscore) {
            return;
        }

        highscores = highscoreManager.addScore(
                playerName,
                score,
                map.getMazeId(),
                map.getMazeName()
        );
        hasSavedHighscore = true;
    }

    private String normalizePlayerName(String name) {
        if (name == null || name.isBlank()) {
            return "Player";
        }
        return name.trim();
    }

    /**
     * Prüft, ob die aktuelle Runde verloren wurde.
     *
     * @return {@code true} im Zustand {@link GameState#GAME_OVER}
     */
    public boolean isGameOver() {
        return gameState == GameState.GAME_OVER;
    }

    /**
     * Prüft die von einer Entität belegten Eckkacheln auf Kollisionen.
     *
     * @param entity zu prüfende Entität
     * @return {@code true}, wenn mindestens eine Ecke blockiert ist
     */
    public boolean isColliding(Entity entity) {
        int tileSize = map.getTileSize();

        int leftTile = (int) (entity.getX() / tileSize);
        int rightTile = (int) (
                (entity.getX() + entity.getSize() - 1) / tileSize
        );
        int topTile = (int) (entity.getY() / tileSize);
        int bottomTile = (int) (
                (entity.getY() + entity.getSize() - 1) / tileSize
        );

        return isBlockedFor(entity, topTile, leftTile)
                || isBlockedFor(entity, topTile, rightTile)
                || isBlockedFor(entity, bottomTile, leftTile)
                || isBlockedFor(entity, bottomTile, rightTile);
    }

    /**
     * Prüft eine kurze Testbewegung in eine Richtung.
     *
     * @param entity zu prüfende Entität
     * @param direction Bewegungsrichtung
     * @return {@code true}, wenn die Bewegung zulässig ist
     */
    public boolean canMove(Entity entity, Direction direction) {
        return canMove(entity, direction, 2);
    }

    /**
     * Prüft eine Bewegung mit frei wählbarer Distanz, ohne die Entität
     * dauerhaft zu verschieben.
     *
     * @param entity zu prüfende Entität
     * @param direction Bewegungsrichtung
     * @param distance Testdistanz in Pixeln
     * @return {@code true}, wenn die Zielposition kollisionsfrei ist
     */
    public boolean canMove(Entity entity, Direction direction, double distance) {
        if (direction == Direction.NONE) {
            return false;
        }

        double oldX = entity.getX();
        double oldY = entity.getY();

        double testX = oldX + direction.getDx() * distance;
        double testY = oldY + direction.getDy() * distance;

        // Die Position wird nur für die Kollisionsprobe verändert und danach
        // unabhängig vom Ergebnis vollständig wiederhergestellt.
        entity.setX(testX);
        entity.setY(testY);

        boolean canMove = !isColliding(entity);

        entity.setX(oldX);
        entity.setY(oldY);

        return canMove;
    }

    private boolean isBlockedFor(Entity entity, int row, int col) {
        if (!map.isInside(row, col)) {
            return true;
        }

        if (map.isWall(row, col)) {
            return true;
        }

        if (!map.isGhostHouse(row, col)) {
            return false;
        }

        // Spieler dürfen das Haus nie betreten. Geister dürfen H/G nur
        // während WAITING_IN_HOUSE und LEAVING_HOUSE verwenden.
        if (entity instanceof Player) {
            return true;
        }

        return entity instanceof Ghost ghost && ghost.isActive();
    }

    private void checkPelletCollection() {
        if (player == null) {
            return;
        }

        for (Pellet pellet : pellets) {
            if (!pellet.isCollected() && isOverlapping(player, pellet)) {
                pellet.collect();
                score += 10;
            }
        }
    }

    private void checkGhostCollision() {
        if (player == null) {
            return;
        }

        for (Entity entity : entities) {
            if (entity instanceof Ghost ghost
                    && ghost.isActive()
                    && isOverlapping(player, ghost)) {
                endGame(GameState.GAME_OVER);
                return;
            }
        }
    }

    private boolean isOverlapping(Entity a, Entity b) {
        return a.getX() < b.getX() + b.getSize()
                && a.getX() + a.getSize() > b.getX()
                && a.getY() < b.getY() + b.getSize()
                && a.getY() + a.getSize() > b.getY();
    }

    /**
     * Erzeugt Pellets ausschliesslich an den in der Karte definierten Stellen.
     */
    public void generatePellets() {
        pellets.clear();

        int tileSize = map.getTileSize();
        for (MazePosition position : map.getPelletPositions()) {
            double x = position.col() * tileSize
                    + (tileSize - Pellet.SIZE) / 2.0;
            double y = position.row() * tileSize
                    + (tileSize - Pellet.SIZE) / 2.0;
            pellets.add(new Pellet(x, y));
        }
    }

    /**
     * Prüft, ob sämtliche Pellets eingesammelt wurden.
     *
     * @return {@code true}, wenn kein nicht eingesammeltes Pellet verbleibt
     */
    public boolean areAllPelletsCollected() {
        for (Pellet pellet : pellets) {
            if (!pellet.isCollected()) {
                return false;
            }
        }
        return true;
    }
}
