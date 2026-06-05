# JPac – Pac-Man Clone in Java

## 1. Projektübersicht

JPac ist eine vereinfachte Umsetzung des bekannten Spiels Pac-Man. Das Projekt wurde in Java entwickelt und verwendet Java Swing für die grafische Darstellung. Ziel des Projekts war es, die Grundlagen der objektorientierten Programmierung, Zustandsmuster (State Pattern), Spielschleifen sowie die Verwaltung von Spiellogik und Benutzeroberfläche zu erlernen.

Der Spieler steuert Pac-Man durch ein Labyrinth und sammelt Pellets ein. Gleichzeitig bewegen sich mehrere Geister durch das Spielfeld und versuchen den Spieler zu verfolgen. Berührt ein Geist den Spieler, endet das Spiel.

---

## 2. Funktionen

Die aktuelle Version beinhaltet folgende Funktionen:

* Startbildschirm mit Spielanleitung
* Steuerung über die Pfeiltasten
* Pellet-Sammelsystem mit Punktezählung
* Vier Geister mit unterschiedlichen Eigenschaften
* Game-Over-Bildschirm
* Gewinnbedingung bei eingesammelten Pellets
* Neustart des Spiels über die Tastatur
* Kameraführung, welche dem Spieler folgt
* Grid-basiertes Bewegungssystem

---

## 3. Architektur

Das Projekt ist in mehrere Komponenten aufgeteilt.

### World

Die Klasse World verwaltet die gesamte Spiellogik. Sie enthält die Spielobjekte, den aktuellen Spielzustand, die Punkteverwaltung sowie die Kollisionsprüfung.

### Entity-System

Alle Spielobjekte basieren auf der Klasse Entity.

Vererbung:

Entity
→ MovingEntity
→ Player
→ Ghost

Dadurch können gemeinsame Eigenschaften wie Position, Grösse und Bewegung zentral verwaltet werden.

### TileMap

Die Klasse TileMap repräsentiert das Spielfeld. Sie speichert die einzelnen Felder des Labyrinths und unterscheidet zwischen:

* WALL
* FLOOR
* GHOST_HOUSE

### Benutzeroberfläche

Die Darstellung erfolgt über:

* GameWindow
* GamePanel
* TileRenderer

Diese Klassen sind für die Anzeige des Spiels verantwortlich und enthalten keine eigentliche Spiellogik.

---

## 4. Ghost-KI

Die Geister verwenden das State Pattern.

GhostState

* IdleState
* ChaseState
* AttackState

Je nach Situation wechseln die Geister zwischen verschiedenen Zuständen.

### IdleState

Der Geist bewegt sich zufällig durch das Labyrinth.

### ChaseState

Der Geist versucht, den Spieler zu verfolgen.

### AttackState

Befindet sich der Spieler sehr nahe am Geist, wird ein aggressiveres Verhalten aktiviert.

Zusätzlich besitzt jeder Geist eine eigene Persönlichkeit:

* RED
* PINK
* CYAN
* ORANGE

Dadurch entstehen unterschiedliche Bewegungsmuster.

---

## 5. Spielablauf

1. Startbildschirm erscheint.
2. Der Spieler startet das Spiel mit ENTER.
3. Pac-Man sammelt Pellets.
4. Die Geister verlassen nacheinander ihr Spawn-Gebiet.
5. Die Geister verfolgen den Spieler.
6. Bei einer Kollision erscheint GAME OVER.
7. Sind alle Pellets eingesammelt, gewinnt der Spieler.
8. Mit R kann das Spiel neu gestartet werden.

---

## 6. Verwendete Konzepte

Im Projekt wurden verschiedene Konzepte der Softwareentwicklung eingesetzt:

* Objektorientierte Programmierung (OOP)
* Vererbung
* Polymorphismus
* Enumerationen (Enums)
* State Pattern
* Game Loop
* Kollisionsprüfung
* Grid-basierte Bewegung
* Trennung von Logik und Darstellung

---

## 7. Fazit

Mit JPac wurde ein funktionsfähiger Pac-Man-Klon entwickelt. Das Projekt verbindet verschiedene Konzepte der Softwareentwicklung in einer praktischen Anwendung. Besonders hilfreich waren die Verwendung von Vererbung zur Strukturierung der Spielobjekte sowie das State Pattern für die Geister-KI.

Die aktuelle Version bildet eine solide Grundlage für zukünftige Erweiterungen wie verbesserte Wegfindung, Animationen, Power-Pellets oder zusätzliche Spielmodi.
