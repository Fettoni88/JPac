package ffhs.jpac;

import ffhs.jpac.ui.GameWindow;

/**
 * Einstiegspunkt der JPac-Anwendung.
 *
 * <p>Die Klasse erzeugt das Hauptfenster und macht es für den Benutzer
 * sichtbar.</p>
 */
public class GameApplication {

    /**
     * Startet die Desktop-Anwendung.
     *
     * @param args Kommandozeilenargumente; sie werden aktuell nicht ausgewertet
     */
    public static void main(String[] args) {
        GameWindow window = new GameWindow();
        window.show();
    }
}
