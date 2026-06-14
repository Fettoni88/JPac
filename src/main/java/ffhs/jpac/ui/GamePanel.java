package ffhs.jpac.ui;

import ffhs.jpac.persistence.HighscoreEntry;
import ffhs.jpac.world.Direction;
import ffhs.jpac.world.Entity;
import ffhs.jpac.world.GameState;
import ffhs.jpac.world.Ghost;
import ffhs.jpac.world.Pellet;
import ffhs.jpac.world.Player;
import ffhs.jpac.world.TileMap;
import ffhs.jpac.world.World;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class GamePanel extends JPanel {

    private static final String[] MENU_OPTIONS = {
            "Start",
            "Highscore",
            "Exit"
    };
    private static final int MENU_START_Y = 280;
    private static final int MENU_SPACING = 60;
    private static final int MAX_NAME_LENGTH = 20;
    private static final int HUD_HEIGHT = 40;

    private final Player player;
    private final TileRenderer tileRenderer;
    private final TileMap map;
    private final World world;
    private final StringBuilder nameInput = new StringBuilder();
    private int selectedMenuOption;

    public GamePanel(Player player, TileMap map, World world) {
        this.player = player;
        this.world = world;
        this.map = map;
        this.tileRenderer = new TileRenderer(map);

        int panelWidth = map.getCols() * map.getTileSize();
        int panelHeight = map.getRows() * map.getTileSize() + HUD_HEIGHT;
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        setBackground(Color.BLACK);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                handleKeyPressed(event);
            }
        });

        MouseAdapter menuMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                handleMenuMouseMoved(event);
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                handleMenuMouseClicked(event);
            }
        };
        addMouseMotionListener(menuMouseAdapter);
        addMouseListener(menuMouseAdapter);
    }

    public void updateInput() {
        // Input is handled directly by the Swing listeners.
    }

    private void handleKeyPressed(KeyEvent event) {
        switch (world.getGameState()) {
            case START_MENU -> handleMenuKeyPressed(event);
            case NAME_INPUT -> handleNameInputKeyPressed(event);
            case HIGHSCORE -> handleHighscoreKeyPressed(event);
            case PLAYING -> handleGameplayKeyPressed(event);
            case GAME_OVER, WIN -> handleEndScreenKeyPressed(event);
        }
    }

    private void handleMenuKeyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_UP) {
            selectedMenuOption =
                    (selectedMenuOption - 1 + MENU_OPTIONS.length)
                            % MENU_OPTIONS.length;
            repaint();
        } else if (event.getKeyCode() == KeyEvent.VK_DOWN) {
            selectedMenuOption =
                    (selectedMenuOption + 1) % MENU_OPTIONS.length;
            repaint();
        } else if (event.getKeyCode() == KeyEvent.VK_ENTER) {
            activateMenuOption(selectedMenuOption);
        }
    }

    private void handleNameInputKeyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            nameInput.setLength(0);
            world.showStartMenu();
            return;
        }

        if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (!nameInput.isEmpty()) {
                nameInput.deleteCharAt(nameInput.length() - 1);
            }
            repaint();
            return;
        }

        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
            world.startGame(nameInput.toString());
            nameInput.setLength(0);
            return;
        }

        char typedCharacter = event.getKeyChar();
        if (!Character.isISOControl(typedCharacter)
                && nameInput.length() < MAX_NAME_LENGTH) {
            nameInput.append(typedCharacter);
            repaint();
        }
    }

    private void handleHighscoreKeyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE
                || event.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            world.showStartMenu();
        }
    }

    private void handleGameplayKeyPressed(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_RIGHT -> player.setDesiredDirection(Direction.RIGHT);
            case KeyEvent.VK_LEFT -> player.setDesiredDirection(Direction.LEFT);
            case KeyEvent.VK_DOWN -> player.setDesiredDirection(Direction.DOWN);
            case KeyEvent.VK_UP -> player.setDesiredDirection(Direction.UP);
            default -> {
                // Other keys do not affect gameplay.
            }
        }
    }

    private void handleEndScreenKeyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_R) {
            world.restartGame();
            selectedMenuOption = 0;
        }
    }

    private void handleMenuMouseMoved(MouseEvent event) {
        if (world.getGameState() != GameState.START_MENU) {
            return;
        }

        for (int index = 0; index < MENU_OPTIONS.length; index++) {
            if (getMenuOptionBounds(index).contains(event.getPoint())) {
                selectedMenuOption = index;
                repaint();
                return;
            }
        }
    }

    private void handleMenuMouseClicked(MouseEvent event) {
        requestFocusInWindow();

        if (world.getGameState() != GameState.START_MENU) {
            return;
        }

        for (int index = 0; index < MENU_OPTIONS.length; index++) {
            if (getMenuOptionBounds(index).contains(event.getPoint())) {
                selectedMenuOption = index;
                activateMenuOption(index);
                return;
            }
        }
    }

    private Rectangle getMenuOptionBounds(int index) {
        int y = MENU_START_Y + index * MENU_SPACING;
        return new Rectangle(getWidth() / 2 - 140, y - 38, 280, 50);
    }

    private void activateMenuOption(int optionIndex) {
        switch (optionIndex) {
            case 0 -> {
                nameInput.setLength(0);
                world.showNameInput();
            }
            case 1 -> world.showHighscores();
            case 2 -> System.exit(0);
            default -> {
                // The selected index always belongs to a menu option.
            }
        }
    }

    private void renderMainMenu(Graphics graphics) {
        graphics.setColor(Color.YELLOW);
        graphics.setFont(graphics.getFont().deriveFont(52f));
        drawCentered(graphics, "JPac", 155);

        for (int index = 0; index < MENU_OPTIONS.length; index++) {
            boolean selected = index == selectedMenuOption;
            graphics.setColor(selected ? Color.YELLOW : Color.WHITE);
            graphics.setFont(graphics.getFont().deriveFont(
                    selected ? 32f : 26f
            ));

            String option = selected
                    ? "> " + MENU_OPTIONS[index] + " <"
                    : MENU_OPTIONS[index];
            drawCentered(
                    graphics,
                    option,
                    MENU_START_Y + index * MENU_SPACING
            );
        }

        graphics.setColor(Color.LIGHT_GRAY);
        graphics.setFont(graphics.getFont().deriveFont(16f));
        drawCentered(
                graphics,
                "Arrow keys / Enter or mouse",
                500
        );
    }

    private void renderNameInput(Graphics graphics) {
        graphics.setColor(Color.YELLOW);
        graphics.setFont(graphics.getFont().deriveFont(42f));
        drawCentered(graphics, "Gib deinen Namen ein", 210);

        graphics.setColor(Color.WHITE);
        graphics.fillRect(getWidth() / 2 - 180, 260, 360, 52);

        graphics.setColor(Color.BLACK);
        graphics.setFont(graphics.getFont().deriveFont(24f));
        String displayedName = nameInput + "_";
        drawCentered(graphics, displayedName, 295);

        graphics.setColor(Color.LIGHT_GRAY);
        graphics.setFont(graphics.getFont().deriveFont(17f));
        drawCentered(graphics, "Enter: Start    ESC: Zurueck", 360);
    }

    private void renderHighscoreScreen(Graphics graphics) {
        graphics.setColor(Color.YELLOW);
        graphics.setFont(graphics.getFont().deriveFont(42f));
        drawCentered(graphics, "Highscores", 105);

        List<HighscoreEntry> highscores = world.getHighscores();
        graphics.setFont(graphics.getFont().deriveFont(22f));

        if (highscores.isEmpty()) {
            graphics.setColor(Color.WHITE);
            drawCentered(graphics, "Noch keine Highscores", 260);
        } else {
            for (int index = 0; index < highscores.size(); index++) {
                HighscoreEntry entry = highscores.get(index);
                graphics.setColor(index == 0 ? Color.YELLOW : Color.WHITE);
                String line = (index + 1) + ". "
                        + entry.getName() + " - " + entry.getScore();
                drawCentered(graphics, line, 155 + index * 34);
            }
        }

        graphics.setColor(Color.LIGHT_GRAY);
        graphics.setFont(graphics.getFont().deriveFont(16f));
        drawCentered(graphics, "ESC oder Backspace: Zurueck", 550);
    }

    private void renderEndScreen(Graphics graphics, String title) {
        graphics.setColor(new Color(0, 0, 0, 210));
        graphics.fillRect(0, 0, getWidth(), getHeight());

        graphics.setColor(Color.YELLOW);
        graphics.setFont(graphics.getFont().deriveFont(42f));
        drawCentered(graphics, title, 75);

        graphics.setColor(Color.WHITE);
        graphics.setFont(graphics.getFont().deriveFont(22f));
        drawCentered(
                graphics,
                "Final Score: " + world.getScore(),
                120
        );

        HighscoreEntry bestHighscore = world.getBestHighscore();
        if (bestHighscore != null) {
            drawCentered(
                    graphics,
                    "Best: " + bestHighscore.getName()
                            + " - " + bestHighscore.getScore(),
                    150
            );
        }

        graphics.setColor(Color.YELLOW);
        graphics.setFont(graphics.getFont().deriveFont(20f));
        drawCentered(graphics, "Top Highscores", 195);

        graphics.setFont(graphics.getFont().deriveFont(17f));
        List<HighscoreEntry> highscores = world.getHighscores();
        for (int index = 0; index < highscores.size(); index++) {
            HighscoreEntry entry = highscores.get(index);
            graphics.setColor(index == 0 ? Color.YELLOW : Color.WHITE);
            String line = (index + 1) + ". "
                    + entry.getName() + " - " + entry.getScore();
            drawCentered(graphics, line, 225 + index * 25);
        }

        graphics.setColor(Color.LIGHT_GRAY);
        graphics.setFont(graphics.getFont().deriveFont(18f));
        drawCentered(graphics, "Press R for Main Menu", 535);
    }

    private void drawCentered(Graphics graphics, String text, int y) {
        int textWidth = graphics.getFontMetrics().stringWidth(text);
        int x = (getWidth() - textWidth) / 2;
        graphics.drawString(text, x, y);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        switch (world.getGameState()) {
            case START_MENU -> {
                renderMainMenu(graphics);
                return;
            }
            case NAME_INPUT -> {
                renderNameInput(graphics);
                return;
            }
            case HIGHSCORE -> {
                renderHighscoreScreen(graphics);
                return;
            }
            default -> {
                // Gameplay and end screens render the world.
            }
        }

        renderMaze(graphics);
        renderPellets(graphics);
        renderEntities(graphics);
        renderHud(graphics);

        if (world.getGameState() == GameState.WIN) {
            renderEndScreen(graphics, "YOU WIN!");
        } else if (world.getGameState() == GameState.GAME_OVER) {
            renderEndScreen(graphics, "GAME OVER");
        }
    }

    private void renderMaze(Graphics graphics) {
        int mazeWidth = map.getCols() * map.getTileSize();
        int mazeHeight = map.getRows() * map.getTileSize();
        Graphics mazeGraphics = graphics.create(
                0,
                HUD_HEIGHT,
                mazeWidth,
                mazeHeight
        );
        tileRenderer.render(
                mazeGraphics,
                0,
                0,
                mazeWidth,
                mazeHeight
        );
        mazeGraphics.dispose();
    }

    private void renderPellets(Graphics graphics) {
        graphics.setColor(Color.YELLOW);

        for (Pellet pellet : world.getPellets()) {
            if (!pellet.isCollected()) {
                int screenX = (int) pellet.getX();
                int screenY = (int) pellet.getY() + HUD_HEIGHT;
                graphics.fillOval(
                        screenX,
                        screenY,
                        pellet.getSize(),
                        pellet.getSize()
                );
            }
        }
    }

    private void renderEntities(Graphics graphics) {
        for (Entity entity : world.getEntities()) {
            int screenX = (int) entity.getX();
            int screenY = (int) entity.getY() + HUD_HEIGHT;

            if (entity instanceof Player) {
                graphics.setColor(Color.YELLOW);
            } else if (entity instanceof Ghost ghost) {
                graphics.setColor(ghost.getColor());
            } else {
                graphics.setColor(Color.WHITE);
            }

            graphics.fillRect(
                    screenX,
                    screenY,
                    entity.getSize(),
                    entity.getSize()
            );
        }
    }

    private void renderHud(Graphics graphics) {
        graphics.setColor(new Color(20, 20, 20));
        graphics.fillRect(0, 0, getWidth(), HUD_HEIGHT);

        graphics.setColor(Color.WHITE);
        graphics.drawString("Score: " + world.getScore(), 15, 25);

        HighscoreEntry bestHighscore = world.getBestHighscore();
        if (bestHighscore != null) {
            String bestText = "Best: " + bestHighscore.getName()
                    + " " + bestHighscore.getScore();
            int textWidth = graphics.getFontMetrics().stringWidth(bestText);
            graphics.drawString(bestText, getWidth() - textWidth - 15, 25);
        }
    }
}
