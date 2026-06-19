# JPac

JPac ist ein Pac-Man-inspiriertes Spiel, das in Java als Semesterprojekt entwickelt wurde. Der Spieler navigiert durch ein kachelbasiertes Labyrinth, sammelt Pellets ein und versucht, vier Geistern auszuweichen — von denen sich jeder anders verhält. Das Ziel war nicht, jedes Detail des originalen Arcade-Spiels nachzubauen, sondern etwas Spielbares und Vollständiges zu entwickeln, während echte Software-Engineering-Konzepte an einem Problem mit klaren Regeln und unmittelbarem Feedback geübt werden.

---

## Warum Pac-Man?

Pac-Man ist eines jener Spiele, bei denen die Regeln auf eine einzige Karteikarte passen — die interessanten Probleme stecken jedoch überall darunter. Es braucht eine Game Loop, ein Kachelsystem, eine Bewegungslogik die sich präzise und reaktionsschnell anfühlt, Gegner die sich voneinander unterscheiden, eine persistente Highscore-Verwaltung, Menüs und Endbildschirme. Jeder Baustein eines funktionierenden Spiels taucht dabei im Kleinen auf.

Es ist ausserdem ein Projekt, das sich innerhalb eines Semesters tatsächlich abschliessen lässt. Der Umfang ist klar begrenzt — kein offenes Spielfeld, keine prozedurale Generierung, keine Netzwerkschicht. Gleichzeitig steckt genug Tiefe darin, um bewusste Architekturentscheidungen zu treffen und ihre Konsequenzen zu spüren. Dieses Gleichgewicht macht es zu einem geeigneten Rahmen für das Modul.

---

## Das Spiel

Nach dem Start gibt man seinen Namen ein, wählt eines der fünf Labyrinthe und legt los. Die Spielfigur startet irgendwo im Labyrinth; die vier Geister sind anfangs im zentralen Geisterhaus in der Mitte der Karte eingeschlossen. Pellets werden durch das Bewegen in den Korridoren eingesammelt. Jedes Pellet ist 10 Punkte wert. Wer alle eingesammelt hat, gewinnt. Wer von einem Geist erwischt wird, verliert.

Die Geister verlassen das Haus nacheinander innerhalb der ersten fünfzehn Sekunden. Rot kommt sofort heraus. Pink folgt nach fünf Sekunden, Blau nach zehn, Orange nach fünfzehn. Einmal draussen, bleibt ein Geist draussen — er kehrt nicht ins Haus zurück.

Was das Spiel interessant hält, ist die Tatsache, dass sich die vier Geister unterschiedlich verhalten. Rot kommt direkt auf den Spieler zu. Pink versucht, ihn abzufangen, indem er nicht die aktuelle Position anvisiert, sondern den Bereich vor ihm. Blau patrouilliert durch das Labyrinth und stürmt auf den Spieler zu, sobald er in Reichweite kommt. Orange wechselt zwischen Verfolgen und Zurückweichen — was ihn auf eine Weise unberechenbar macht, die die anderen nicht haben. Den Unterschied merkt man nach wenigen Runden.

Am Ende einer Runde — egal ob gewonnen oder verloren — wird der Punktestand zusammen mit dem Spielernamen und dem gespielten Labyrinth in einer Top-10-Highscore-Liste gespeichert. Die Liste bleibt zwischen den Sitzungen erhalten und ist sowohl auf dem Endbildschirm als auch im Hauptmenü einsehbar.

---

## Die fünf Labyrinthe

Die Wahl des Labyrinths ist Teil des Spiels. Die fünf enthaltenen Labyrinthe unterscheiden sich in Layout und Charakter — manche enger und verwinkelter, andere offener, einige mit mehr Sackgassen und Engpässen. Jedes verändert, wie nützlich die verschiedenen Geisterpersönlichkeiten tatsächlich sind, was dem Spiel Abwechslung verleiht, ohne neue Systeme zu erfordern.

Labyrinthe sind als JSON-Dateien definiert und können ausserhalb des Codes bearbeitet oder erweitert werden. Der Loader validiert jedes Labyrinth beim Laden — er prüft Abmessungen, erforderliche Spawnpunkte, geschlossene Aussenwände und die Verbindung aller erreichbaren Kacheln — sodass eine fehlerhafte oder unvollständige Labyrinth-Datei abgefangen wird, bevor das Spiel überhaupt startet.

---

## Projektziele

Das Projekt diente als Vehikel zum Üben von Software Engineering — nicht nur Programmieren. Die wichtigsten Themen, die dabei abgedeckt werden:

**Objektorientiertes Design.** Alles, was sich im Spiel bewegt, erbt von einer gemeinsamen `Entity`-Basis. Spieler und Geister teilen sich eine gemeinsame `MovingEntity`-Zwischenstufe. Die vier Geister nutzen dieselbe `Ghost`-Klasse, unterscheiden sich aber durch austauschbare Strategie- und Zustandsobjekte — nicht durch Vererbung.

**Verhaltensmuster.** Die Geister-KI kombiniert zwei Entwurfsmuster. Das *Strategy Pattern* trennt die vier Persönlichkeiten — jede ist eine eigenständige Klasse, die entscheidet, wohin ein Geist steuert. Das *State Pattern* trennt, was ein Geist gerade tut — wandern, verfolgen, angreifen — von der Logik der Zielauswahl. Beide Schichten fügen sich sauber zusammen.

**Game Loop und Timing.** Die Game Loop läuft mit einem festen Zielwert von 60 FPS, übergibt allen Entitäten pro Frame die tatsächlich verstrichene Zeit (Delta Time) und hält Spiellogik und Rendering strikt getrennt.

**Datengetriebene Inhalte.** Labyrinthe liegen als JSON ausserhalb des Codes. Das Hinzufügen oder Ändern eines Labyrinths erfordert kein Neukompilieren.

**Persistenz.** Das Highscore-System liest und schreibt eine JSON-Datei, behandelt fehlende oder beschädigte Dateien fehlertolerant und hält die Top-10-Einträge sortiert nach Punktestand.

**Trennung der Zuständigkeiten.** Die Spiellogik hat keine Abhängigkeit vom Rendering. Die UI liest aus der Spielwelt und zeichnet sie — sie verändert keinen Zustand. Das machte das Testen unkompliziert: Die meisten der 72 automatisierten Tests laufen direkt gegen die Spiellogik, ohne dass Swing-Komponenten beteiligt sind.

---

## Die grössten Herausforderungen

**Die Geisterbewegung richtig anfühlen lassen.** Die Geister nutzen BFS-Pfadfindung, um sich durch das Labyrinth zu bewegen — aber rohe BFS-Bewegung wirkt mechanisch. Das eigentliche Problem war, Geister dazu zu bringen, ihre aktuelle Richtung beizubehalten statt zu wenden, Hin-und-Her-Oszillationen an Kreuzungen zu vermeiden und erst in echten Sackgassen umzukehren. Das sind kleine Einzelentscheidungen, aber sie sind der Unterschied zwischen einem Geist, der aussieht als hätte er ein Ziel, und einem, der ziellos herumläuft.

**Das Geisterhaus-Austrittssystem.** Geister innerhalb des Hauses müssen den Ausgang zuverlässig finden, aber das Haus ist vom Rest des Labyrinths durch Wände abgetrennt. Die Austrittslogik läuft über ein eigenes BFS auf den Hauskacheln, unabhängig vom normalen Bewegungssystem — sodass ein Geist von jeder Position innerhalb des Hauses aus immer den richtigen Weg hinaus findet.

**Kachelbasierte Bewegung mit flüssigem Spielgefühl.** Bewegliche Entitäten rasten an Kachelmitten ein, was die Gitterlogik konsistent hält — aber die kontinuierliche Bewegung zwischen den Mitten muss flüssig aussehen. Die Eingabe des Spielers wird 250 Millisekunden lang zwischengespeichert, sodass eine Richtungseingabe kurz vor einer Ecke noch registriert wird. Ein kleines Detail, das die Steuerung erheblich reaktionsschneller wirken lässt.

**Labyrinth-Validierung.** Es ist leicht, eine Labyrinth-JSON zu schreiben, die valide aussieht, aber es nicht ist — mit einem unerreichbaren Abschnitt, einem vom Haus getrennten Geister-Spawn oder einem Spielerstart, von dem aus nicht alle Pellets erreichbar sind. Der Loader führt bei jedem Laden drei separate BFS-Durchläufe durch, um solche Fälle abzufangen und eine verständliche Fehlermeldung auszugeben, bevor zur Laufzeit irgendetwas kaputtgeht.

---

## Bewusst weggelassene Features

Der Fokus dieses Projekts lag auf einem stabilen, gut strukturierten und wartbaren Spiel. Mehrere Features des originalen Pac-Man wurden bewusst nicht umgesetzt.

**Power-Pellets und Frightened Mode.** Die grossen Pellets, die Geister vorübergehend verletzbar machen, erfordern einen zusätzlichen Geisterzustand, koordinierte zeitgesteuerte Übergänge bei allen vier Geistern gleichzeitig und Änderungen am Punktesystem. Das Symbol `'o'` ist im Maze-Loader bereits definiert und für zukünftige Nutzung reserviert — das Spielverhalten dahinter wurde jedoch weggelassen.

**Bonus-Früchte.** Das Objekt, das in der Mitte des Labyrinths nach einer bestimmten Anzahl eingesammelter Pellets erscheint, erfordert einen Timer, ein separates Render-Element und zusätzliche Punktelogik. Es wurde zugunsten eines einfachen Punktesystems gestrichen.

**Lebens-System.** JPac endet sofort beim ersten Kontakt mit einem Geist. Mehrere Leben würden das Verfolgen der verbleibenden Leben über Runden hinweg, eine Respawn-Sequenz und das Erhalten des aktuellen Pellet-Zustands nach einem Tod erfordern. Das fügt Komplexität hinzu, ohne zu den Lernzielen des Projekts beizutragen.

**Sound und Musik.** Es ist kein Audio implementiert. Die Sounds des Originalspiels sind ein wesentlicher Teil seiner Atmosphäre, aber das Hinzufügen von Audio hätte eine Asset-Pipeline und eine Audioverwaltungsschicht erfordert, die ausserhalb des Projektumfangs lagen.

**Animationen.** Entitäten werden als einfarbige Rechtecke gerendert. Sprite-basierte Animationen wurden früh gestrichen, um das Rendering einfach zu halten und den Fokus auf Spiellogik und Architektur zu legen.

---

## Mögliche zukünftige Erweiterungen

- **Power-Pellets** — das Symbol ist bereits im Labyrinth-Format vorhanden; Frightened Mode umzusetzen bedeutet, einen Geisterzustand und Timer-Logik hinzuzufügen
- **Verbesserte Geister-KI** — intelligenteres Verhalten in offenen Bereichen, bessere Koordination zwischen den Geistern
- **Lebens-System** — drei Leben vor Game Over, mit kurzer Respawn-Verzögerung und Unverwundbarkeits-Fenster
- **Schwierigkeitseinstellungen** — anpassbare Geistergeschwindigkeit und Freigabe-Timing, wählbar im Menü
- **Sound-Effekte** — Sounds für Pellet-Einsammeln, Game Over und Sieg
- **Animierte Sprites** — Ersetzen der einfarbigen Rechtecke durch echte Charaktergrafiken
- **Labyrinth-Editor** — ein Werkzeug zum Erstellen neuer Labyrinthe im JSON-Format, das der Loader erwartet
- **Weitere Labyrinthe** — das Ladesystem ist vollständig datengetrieben; ein neues Labyrinth ist nur eine neue JSON-Datei

---