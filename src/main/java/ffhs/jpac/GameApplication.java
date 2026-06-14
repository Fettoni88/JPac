package ffhs.jpac;

import ffhs.jpac.ui.GameWindow;
import ffhs.jpac.ui.PlayerNameDialog;

public class GameApplication {

    public static void main(String[] args) {
        String playerName = PlayerNameDialog.askPlayerName();
        GameWindow window = new GameWindow(playerName);
        window.show();
    }
}
