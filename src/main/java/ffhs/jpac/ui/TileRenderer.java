package ffhs.jpac.ui;

import ffhs.jpac.world.TileMap;
import ffhs.jpac.world.TileType;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Zeichnet den sichtbaren Ausschnitt einer {@link TileMap}.
 */
public class TileRenderer {

    private final TileMap map;

    /**
     * Erstellt einen Renderer für die angegebene Karte.
     *
     * @param map darzustellende Karte
     */
    public TileRenderer(TileMap map) {
        this.map = map;
    }

    /**
     * Zeichnet alle Kacheln innerhalb des sichtbaren Rechtecks.
     *
     * @param g Zeichenkontext
     * @param cameraX horizontale Position des sichtbaren Ausschnitts
     * @param cameraY vertikale Position des sichtbaren Ausschnitts
     * @param screenWidth Breite des sichtbaren Bereichs
     * @param screenHeight Höhe des sichtbaren Bereichs
     */
    public void render(
            Graphics g,
            int cameraX,
            int cameraY,
            int screenWidth,
            int screenHeight
    ) {

        int tileSize = map.getTileSize();

        int firstCol = cameraX / tileSize;
        int lastCol = (cameraX + screenWidth) / tileSize;

        int firstRow = cameraY / tileSize;
        int lastRow = (cameraY + screenHeight) / tileSize;

        for (int row = firstRow; row <= lastRow; row++) {
            for (int col = firstCol; col <= lastCol; col++) {

                if (row < 0
                        || col < 0
                        || row >= map.getRows()
                        || col >= map.getCols()) {
                    continue;
                }

                TileType tile = map.getTile(row, col);

                int worldX = col * tileSize;
                int worldY = row * tileSize;

                int screenX = worldX - cameraX;
                int screenY = worldY - cameraY;

                switch (tile) {
                    case WALL -> g.setColor(Color.DARK_GRAY);
                    case FLOOR -> g.setColor(Color.GRAY);
                    case GHOST_HOUSE -> g.setColor(new Color(70, 45, 70));
                }

                g.fillRect(screenX, screenY, tileSize, tileSize);
            }
        }
    }
}
