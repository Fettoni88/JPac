package ch.fettoni.rpg.ui;

import ch.fettoni.rpg.world.TileMap;
import ch.fettoni.rpg.world.TileType;

import java.awt.Color;
import java.awt.Graphics;

public class TileRenderer {

    private final TileMap map;

    public TileRenderer(TileMap map) {
        this.map = map;
    }

    public void render(Graphics g, int cameraX, int cameraY, int screenWidth, int screenHeight) {

        int tileSize = map.getTileSize();

        int firstCol = cameraX / tileSize;
        int lastCol = (cameraX + screenWidth) / tileSize;

        int firstRow = cameraY / tileSize;
        int lastRow = (cameraY + screenHeight) / tileSize;

        for (int row = firstRow; row <= lastRow; row++) {
            for (int col = firstCol; col <= lastCol; col++) {

                if (row < 0 || col < 0 || row >= map.getRows() || col >= map.getCols()) {
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
                }

                g.fillRect(screenX, screenY, tileSize, tileSize);
            }
        }
    }
}