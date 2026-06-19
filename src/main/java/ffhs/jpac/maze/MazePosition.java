package ffhs.jpac.maze;

/**
 * Unveränderliche Position innerhalb des kachelbasierten Labyrinthrasters.
 *
 * @param row nullbasierter Zeilenindex
 * @param col nullbasierter Spaltenindex
 */
public record MazePosition(int row, int col) {
}
