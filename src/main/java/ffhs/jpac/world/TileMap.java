package ffhs.jpac.world;

import ffhs.jpac.maze.MazeData;
import ffhs.jpac.maze.MazeLoader;
import ffhs.jpac.maze.MazePosition;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Stellt das geladene Labyrinth als Kachelraster bereit.
 *
 * <p>Die Klasse übernimmt die Symbolumwandlung, Koordinatenumrechnung,
 * Spawninformationen und kartenbezogene Wegabfragen.</p>
 */
public class TileMap {

    private static final int TILE_SIZE = 24;

    private static final int FLOOR = 0;
    private static final int WALL = 1;
    private static final int GHOST_HOUSE = 2;

    private final TileType[][] map;
    private final boolean[][] pelletTiles;
    private final List<MazePosition> pelletPositions = new ArrayList<>();
    private final List<MazePosition> ghostSpawns = new ArrayList<>();
    private final List<MazePosition> ghostHouseExits = new ArrayList<>();
    private String mazeId = "legacy";
    private String mazeName = "Legacy Maze";
    private MazePosition playerSpawn;

    /**
     * Lädt eine Karte aus einer Text- oder JSON-Ressource.
     *
     * @param resourcePath Ressourcenpfad der Karte
     * @throws IllegalArgumentException wenn die Ressource nicht gelesen oder
     *         validiert werden kann
     */
    public TileMap(String resourcePath) {
        if (resourcePath.endsWith(".json")) {
            loadMazeData(MazeLoader.load(resourcePath));
        } else {
            loadLegacyMap(resourcePath);
        }

        map = loadedMap;
        pelletTiles = loadedPelletTiles;
    }

    /**
     * Erstellt eine Karte aus bereits geladenen Labyrinthdaten.
     *
     * @param mazeData zu konvertierende Labyrinthdaten
     * @throws IllegalArgumentException wenn die Daten ungültig sind
     */
    public TileMap(MazeData mazeData) {
        MazeLoader.validate(mazeData);
        loadMazeData(mazeData);
        map = loadedMap;
        pelletTiles = loadedPelletTiles;
    }

    private TileType[][] loadedMap;
    private boolean[][] loadedPelletTiles;

    private void loadMazeData(MazeData mazeData) {
        mazeId = mazeData.getId();
        mazeName = mazeData.getName();
        List<String> pattern = mazeData.getPattern();
        loadedMap = new TileType[pattern.size()][pattern.getFirst().length()];
        loadedPelletTiles =
                new boolean[pattern.size()][pattern.getFirst().length()];

        for (int row = 0; row < pattern.size(); row++) {
            for (int col = 0; col < pattern.get(row).length(); col++) {
                char symbol = pattern.get(row).charAt(col);
                // Spawn- und Hauszeichen werden für die Kollision auf die
                // drei fachlichen Kacheltypen reduziert.
                loadedMap[row][col] = switch (symbol) {
                    case '#' -> TileType.WALL;
                    case 'G', 'H' -> TileType.GHOST_HOUSE;
                    default -> TileType.FLOOR;
                };

                if (symbol == '.' || symbol == 'o') {
                    loadedPelletTiles[row][col] = true;
                    pelletPositions.add(new MazePosition(row, col));
                } else if (symbol == 'P') {
                    playerSpawn = new MazePosition(row, col);
                } else if (symbol == 'G') {
                    ghostSpawns.add(new MazePosition(row, col));
                } else if (symbol == 'E') {
                    ghostHouseExits.add(new MazePosition(row, col));
                }
            }
        }
    }

    private void loadLegacyMap(String resourcePath) {
        try {
            InputStream input = getClass().getResourceAsStream(resourcePath);
            if (input == null) {
                throw new IllegalArgumentException(
                        "Map resource not found: " + resourcePath
                );
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input)
            );
            List<TileType[]> rows = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");
                TileType[] row = new TileType[tokens.length];

                for (int col = 0; col < tokens.length; col++) {
                    int value = Integer.parseInt(tokens[col]);
                    row[col] = switch (value) {
                        case WALL -> TileType.WALL;
                        case GHOST_HOUSE -> TileType.GHOST_HOUSE;
                        default -> TileType.FLOOR;
                    };
                }

                rows.add(row);
            }

            loadedMap = rows.toArray(TileType[][]::new);
            loadedPelletTiles =
                    new boolean[loadedMap.length][loadedMap[0].length];

            for (int row = 0; row < loadedMap.length; row++) {
                for (int col = 0; col < loadedMap[row].length; col++) {
                    if (loadedMap[row][col] == TileType.FLOOR) {
                        loadedPelletTiles[row][col] = true;
                        pelletPositions.add(new MazePosition(row, col));
                    }
                }
            }
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Could not load map: " + resourcePath,
                    exception
            );
        }
    }

    /**
     * Gibt den Typ einer Kachel zurück.
     *
     * @param row Zeilenindex
     * @param col Spaltenindex
     * @return Kacheltyp
     */
    public TileType getTile(int row, int col) {
        return map[row][col];
    }

    /**
     * Prüft, ob eine Kachel eine Wand ist.
     *
     * @param row Zeilenindex
     * @param col Spaltenindex
     * @return {@code true} für eine Wand
     */
    public boolean isWall(int row, int col) {
        return map[row][col].isSolid();
    }

    /**
     * Prüft, ob eine Kachel zum Geisterhaus gehört.
     *
     * @param row Zeilenindex
     * @param col Spaltenindex
     * @return {@code true} für Hausboden oder Geistspawn
     */
    public boolean isGhostHouse(int row, int col) {
        return map[row][col] == TileType.GHOST_HOUSE;
    }

    /**
     * Prüft, ob die Karte ein Geisterhaus enthält.
     *
     * @return {@code true}, wenn mindestens eine Hauskachel vorhanden ist
     */
    public boolean hasGhostHouse() {
        for (TileType[] row : map) {
            for (TileType tile : row) {
                if (tile == TileType.GHOST_HOUSE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Prüft, ob auf einer Kachel ursprünglich ein Pellet definiert ist.
     *
     * @param row Zeilenindex
     * @param col Spaltenindex
     * @return {@code true} für eine Pelletkachel
     */
    public boolean isPelletTile(int row, int col) {
        return pelletTiles[row][col];
    }

    /**
     * Prüft Rasterkoordinaten auf Gültigkeit.
     *
     * @param row Zeilenindex
     * @param col Spaltenindex
     * @return {@code true}, wenn die Position innerhalb der Karte liegt
     */
    public boolean isInside(int row, int col) {
        return row >= 0 && col >= 0 && row < getRows() && col < getCols();
    }

    /**
     * Gibt die Anzahl der Kartenzeilen zurück.
     *
     * @return Anzahl Kartenzeilen
     */
    public int getRows() {
        return map.length;
    }

    /**
     * Gibt die Anzahl der Kartenspalten zurück.
     *
     * @return Anzahl Kartenspalten
     */
    public int getCols() {
        return map[0].length;
    }

    /**
     * Gibt die einheitliche Kachelgrösse zurück.
     *
     * @return Kachelgrösse in Pixeln
     */
    public int getTileSize() {
        return TILE_SIZE;
    }

    /**
     * Wandelt eine horizontale Pixelkoordinate in eine Spalte um.
     *
     * @param pixelX horizontale Pixelkoordinate
     * @return nullbasierter Spaltenindex
     */
    public int getTileColFromPixel(double pixelX) {
        return (int) Math.floor(pixelX / TILE_SIZE);
    }

    /**
     * Wandelt eine vertikale Pixelkoordinate in eine Zeile um.
     *
     * @param pixelY vertikale Pixelkoordinate
     * @return nullbasierter Zeilenindex
     */
    public int getTileRowFromPixel(double pixelY) {
        return (int) Math.floor(pixelY / TILE_SIZE);
    }

    /**
     * Berechnet die horizontale Mitte einer Kachel.
     *
     * @param col Spaltenindex
     * @return horizontale Pixelkoordinate des Mittelpunkts
     */
    public double getTileCenterX(int col) {
        return col * TILE_SIZE + TILE_SIZE / 2.0;
    }

    /**
     * Berechnet die vertikale Mitte einer Kachel.
     *
     * @param row Zeilenindex
     * @return vertikale Pixelkoordinate des Mittelpunkts
     */
    public double getTileCenterY(int row) {
        return row * TILE_SIZE + TILE_SIZE / 2.0;
    }

    /**
     * Prüft, ob eine Kachel vom Spieler betreten werden darf.
     *
     * @param row Zeilenindex
     * @param col Spaltenindex
     * @return {@code true} für normalen, gültigen Boden
     */
    public boolean isWalkableForPlayer(int row, int col) {
        return isInside(row, col)
                && !isWall(row, col)
                && !isGhostHouse(row, col);
    }

    /**
     * Prüft, ob ein aktiver Geist eine Kachel betreten darf.
     *
     * <p>Aktive Geister dürfen wie der Spieler nicht in das Geisterhaus
     * zurückkehren.</p>
     *
     * @param row Zeilenindex
     * @param col Spaltenindex
     * @return {@code true} für eine begehbare Kachel
     */
    public boolean isWalkableForActiveGhost(int row, int col) {
        return isWalkableForPlayer(row, col);
    }

    /**
     * Ermittelt vier über die Karte verteilte Patrouillenziele.
     *
     * @return unveränderliche Liste erreichbarer Zielkacheln
     */
    public List<MazePosition> getPatrolTargets() {
        List<MazePosition> targets = new ArrayList<>();
        addClosestWalkableTarget(targets, 1, 1);
        addClosestWalkableTarget(targets, 1, getCols() - 2);
        addClosestWalkableTarget(targets, getRows() - 2, getCols() - 2);
        addClosestWalkableTarget(targets, getRows() - 2, 1);
        return List.copyOf(targets);
    }

    /**
     * Wählt das vom Spieler am weitesten entfernte erreichbare Patrouillenziel.
     *
     * @param ghostPosition aktuelle Geistposition
     * @param playerPosition aktuelle Spielerposition
     * @return Fluchtziel; ersatzweise die aktuelle Geistposition
     */
    public MazePosition getFarthestPatrolTarget(
            MazePosition ghostPosition,
            MazePosition playerPosition
    ) {
        MazePosition bestTarget = ghostPosition;
        int bestDistance = -1;

        for (MazePosition target : getPatrolTargets()) {
            if (!hasActiveGhostPath(
                    ghostPosition.row(),
                    ghostPosition.col(),
                    target.row(),
                    target.col()
            )) {
                continue;
            }

            int distance = Math.abs(target.row() - playerPosition.row())
                    + Math.abs(target.col() - playerPosition.col());
            if (distance > bestDistance) {
                bestDistance = distance;
                bestTarget = target;
            }
        }

        return bestTarget;
    }

    private void addClosestWalkableTarget(
            List<MazePosition> targets,
            int preferredRow,
            int preferredCol
    ) {
        MazePosition bestPosition = null;
        int bestDistance = Integer.MAX_VALUE;

        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getCols(); col++) {
                MazePosition position = new MazePosition(row, col);
                if (!isWalkableForActiveGhost(row, col)
                        || targets.contains(position)) {
                    continue;
                }

                int distance = Math.abs(row - preferredRow)
                        + Math.abs(col - preferredCol);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestPosition = position;
                }
            }
        }

        if (bestPosition != null) {
            targets.add(bestPosition);
        }
    }

    /**
     * Gibt die technische Kartenkennung zurück.
     *
     * @return technische Kennung der Karte
     */
    public String getMazeId() {
        return mazeId;
    }

    /**
     * Gibt den Anzeigenamen der Karte zurück.
     *
     * @return Anzeigename der Karte
     */
    public String getMazeName() {
        return mazeName;
    }

    /**
     * Gibt den Spieler-Spawnpunkt zurück.
     *
     * @return Spawnposition des Spielers
     * @throws IllegalStateException wenn die Karte keinen Spieler-Spawn enthält
     */
    public MazePosition getPlayerSpawn() {
        if (playerSpawn == null) {
            throw new IllegalStateException("Map has no player spawn");
        }
        return playerSpawn;
    }

    /**
     * Gibt alle Geistspawnpunkte zurück.
     *
     * @return unveränderliche Liste der Geistspawnpunkte
     */
    public List<MazePosition> getGhostSpawns() {
        return List.copyOf(ghostSpawns);
    }

    /**
     * Gibt alle definierten Pelletpositionen zurück.
     *
     * @return unveränderliche Liste der Pelletpositionen
     */
    public List<MazePosition> getPelletPositions() {
        return List.copyOf(pelletPositions);
    }

    /**
     * Gibt alle expliziten Geisterhausausgänge zurück.
     *
     * @return unveränderliche Liste der Geisterhausausgänge
     */
    public List<MazePosition> getGhostHouseExits() {
        return List.copyOf(ghostHouseExits);
    }

    /**
     * Prüft, ob eine Kachel als Hausausgang definiert ist.
     *
     * @param row Zeilenindex
     * @param col Spaltenindex
     * @return {@code true} für eine Ausgangskachel
     */
    public boolean isGhostHouseExit(int row, int col) {
        if (!ghostHouseExits.isEmpty()) {
            return ghostHouseExits.contains(new MazePosition(row, col));
        }

        int[] exit = findGhostHouseExit();
        return row == exit[2] && col == exit[3];
    }

    /**
     * Bestimmt den nächsten Schritt zum nächstgelegenen Hausausgang.
     *
     * @param startRow aktuelle Zeile
     * @param startCol aktuelle Spalte
     * @param currentDirection aktuelle Bewegungsrichtung
     * @return nächste Richtung oder {@link Direction#NONE}
     */
    public Direction getDirectionTowardNearestGhostHouseExit(
            int startRow,
            int startCol,
            Direction currentDirection
    ) {
        return new GhostNavigator(this).directionToNearestHouseExit(
                startRow,
                startCol,
                currentDirection
        );
    }

    /**
     * Prüft, ob zwischen zwei Kacheln ein Weg für aktive Geister besteht.
     *
     * @param startRow Startzeile
     * @param startCol Startspalte
     * @param targetRow Zielzeile
     * @param targetCol Zielspalte
     * @return {@code true}, wenn ein Weg existiert
     */
    public boolean hasActiveGhostPath(
            int startRow,
            int startCol,
            int targetRow,
            int targetCol
    ) {
        return new GhostNavigator(this).hasActivePath(
                startRow,
                startCol,
                targetRow,
                targetCol
        );
    }

    /**
     * Bestimmt die nächste Richtung zu einer Zielkachel.
     *
     * @param startRow Startzeile
     * @param startCol Startspalte
     * @param targetRow Zielzeile
     * @param targetCol Zielspalte
     * @param currentDirection aktuelle Bewegungsrichtung
     * @return nächste Richtung oder {@link Direction#NONE}
     */
    public Direction getDirectionTowardTarget(
            int startRow,
            int startCol,
            int targetRow,
            int targetCol,
            Direction currentDirection
    ) {
        return new GhostNavigator(this).directionToTarget(
                startRow,
                startCol,
                targetRow,
                targetCol,
                currentDirection
        );
    }

    /**
     * Sucht den Haus- und Ausgangsteil des nächstgelegenen Ausgangs.
     *
     * @return Array mit Hauszeile, Hausspalte, Ausgangszeile und Ausgangsspalte
     * @throws IllegalStateException wenn kein Ausgang bestimmt werden kann
     */
    public int[] findGhostHouseExit() {
        int[] center = findGhostHouseCenter();
        return findGhostHouseExit(
                getTileCenterX(center[1]),
                getTileCenterY(center[0])
        );
    }

    /**
     * Sucht den zur Pixelposition nächstgelegenen Geisterhausausgang.
     *
     * @param pixelX horizontale Pixelposition
     * @param pixelY vertikale Pixelposition
     * @return Array mit Hauszeile, Hausspalte, Ausgangszeile und Ausgangsspalte
     * @throws IllegalStateException wenn kein Ausgang bestimmt werden kann
     */
    public int[] findGhostHouseExit(double pixelX, double pixelY) {
        if (!ghostHouseExits.isEmpty()) {
            int startRow = getTileRowFromPixel(pixelY);
            int startCol = getTileColFromPixel(pixelX);
            MazePosition exit = ghostHouseExits.stream()
                    .min((first, second) -> Integer.compare(
                            distance(startRow, startCol, first),
                            distance(startRow, startCol, second)
                    ))
                    .orElseThrow();

            for (Direction direction : List.of(
                    Direction.UP,
                    Direction.DOWN,
                    Direction.LEFT,
                    Direction.RIGHT
            )) {
                int houseRow = exit.row() - direction.getDy();
                int houseCol = exit.col() - direction.getDx();

                if (isInside(houseRow, houseCol)
                        && isGhostHouse(houseRow, houseCol)) {
                    return new int[]{
                            houseRow,
                            houseCol,
                            exit.row(),
                            exit.col()
                    };
                }
            }

            throw new IllegalStateException(
                    "Ghost house exit is not next to ghost house floor"
            );
        }

        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getCols(); col++) {
                if (!isGhostHouse(row, col)) {
                    continue;
                }

                for (Direction direction : List.of(
                        Direction.UP,
                        Direction.DOWN,
                        Direction.LEFT,
                        Direction.RIGHT
                )) {
                    int outsideRow = row + direction.getDy();
                    int outsideCol = col + direction.getDx();

                    if (isInside(outsideRow, outsideCol)
                            && getTile(outsideRow, outsideCol)
                            == TileType.FLOOR) {
                        return new int[]{
                                row,
                                col,
                                outsideRow,
                                outsideCol
                        };
                    }
                }
            }
        }

        throw new IllegalStateException("Ghost house has no exit");
    }

    private int distance(
            int startRow,
            int startCol,
            MazePosition position
    ) {
        return Math.abs(startRow - position.row())
                + Math.abs(startCol - position.col());
    }

    /**
     * Berechnet die mittlere Kachel des Geisterhauses.
     *
     * @return Array mit Zeile und Spalte des Hausmittelpunkts
     * @throws IllegalStateException wenn kein Geisterhaus vorhanden ist
     */
    public int[] findGhostHouseCenter() {
        int rowSum = 0;
        int colSum = 0;
        int tileCount = 0;

        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getCols(); col++) {
                if (isGhostHouse(row, col)) {
                    rowSum += row;
                    colSum += col;
                    tileCount++;
                }
            }
        }

        if (tileCount == 0) {
            throw new IllegalStateException("Map has no ghost house");
        }

        return new int[]{
                Math.round((float) rowSum / tileCount),
                Math.round((float) colSum / tileCount)
        };
    }
}
