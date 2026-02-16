package ch.fettoni.rpg.world;

public class TileMap {

    private final int tileSize = 32;

    private final int[][] map = {
            {1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1}
    };

    public int getTile(int row, int col) {
        return map[row][col];
    }

    public int getRows() {
        return map.length;
    }

    public int getCols() {
        return map[0].length;
    }

    public int getTileSize() {
        return tileSize;
    }
}