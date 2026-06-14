package ffhs.jpac.ui;

import javax.swing.JOptionPane;

public final class PlayerNameDialog {

    private static final String DEFAULT_NAME = "Player";

    private PlayerNameDialog() {
    }

    public static String askPlayerName() {
        String name = JOptionPane.showInputDialog(
                null,
                "Enter your player name:",
                "JPac",
                JOptionPane.QUESTION_MESSAGE
        );

        if (name == null || name.isBlank()) {
            return DEFAULT_NAME;
        }

        return name.trim();
    }
}
