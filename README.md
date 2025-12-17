# Time Task Tracker

Eine macOS Menüleisten-App zur Zeiterfassung für Aufgaben, entwickelt mit Kotlin und Compose Multiplatform.

## Features

- ⏱️ **Zeiterfassung**: Tracke die Zeit, die du mit Aufgaben verbringst
- 📊 **Verlauf**: Zeige abgeschlossene Aufgaben mit Zeitangaben an
- 🎯 **Menüleisten-Integration**: Sieh die aktuelle Zeit direkt in der macOS-Menüleiste
- 🎨 **Moderne UI**: Entwickelt mit Compose Multiplatform und Material 3

## Voraussetzungen

- Java JDK 17 oder höher
- macOS (für die Menüleisten-Funktionalität)

## Lokale Ausführung

Zum Starten der Anwendung:

```bash
./gradlew run
```

Die App erscheint in der Menüleiste. Klicke auf das Icon, um das Hauptfenster zu öffnen.

## DMG erstellen

Um eine DMG-Datei für die Distribution zu erstellen:

```bash
./gradlew packageDmg
```

Die DMG-Datei wird in `build/compose/binaries/main/dmg/` erstellt.

## Verwendung

1. **Aufgabe starten**: Gib einen Aufgabennamen ein und klicke auf "Start"
2. **Zeit tracken**: Die Zeit wird automatisch in der Menüleiste angezeigt
3. **Aufgabe beenden**: Klicke auf "Stop", um die Aufgabe zu beenden
4. **Verlauf ansehen**: Abgeschlossene Aufgaben werden im Verlauf angezeigt

## Technologie-Stack

- **Kotlin** 2.1.0
- **Compose Multiplatform** 1.7.1
- **Gradle** 8.11.1
- **Material 3** Design System

## Lizenz

MIT
