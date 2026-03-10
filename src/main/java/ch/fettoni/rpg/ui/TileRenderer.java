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

    public void render(Graphics g) {

        int tileSize = map.getTileSize();

        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {

                TileType tile = map.getTile(row, col);

                switch (tile) {

                    case WALL -> g.setColor(Color.DARK_GRAY);

                    case FLOOR -> g.setColor(Color.GRAY);
                }

                g.fillRect(
                        col * tileSize,
                        row * tileSize,
                        tileSize,
                        tileSize
                );
            }
        }
    }
}