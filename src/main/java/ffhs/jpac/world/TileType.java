package ffhs.jpac.world;

public enum TileType {

    FLOOR(false),
    GHOST_HOUSE(false),
    WALL(true);

    private final boolean solid;

    TileType(boolean solid) {
        this.solid = solid;
    }

    public boolean isSolid() {
        return solid;
    }
}
