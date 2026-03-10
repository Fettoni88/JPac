package ch.fettoni.rpg.world;

public enum TileType {

    FLOOR(false),
    WALL(true);

    private final boolean solid;

    TileType(boolean solid) {
        this.solid = solid;
    }

    public boolean isSolid() {
        return solid;
    }
}