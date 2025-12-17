# Time Task Tracker

Eine macOS Menüleisten-App zur Zeiterfassung für Aufgaben, entwickelt mit Kotlin und Compose Multiplatform.

## Features

- ⏱️ **Zeiterfassung**: Tracke die Zeit, die du mit Aufgaben verbringst
- 📊 **Verlauf**: Zeige abgeschlossene Aufgaben mit Zeitangaben an
- 🎯 **Menüleisten-Integration**: Sieh die aktuelle Zeit direkt in der macOS-Menüleiste
- 🎨 **Moderne UI**: Entwickelt mit Compose Multiplatform und Material 3
- 💾 **Persistente Speicherung**: Alle Aufgaben werden in einer SQLite-Datenbank gespeichert und überleben Anwendungsneustarts
- 🔄 **Blinkender Indikator**: Der Doppelpunkt in der Menüleiste blinkt, wenn eine Zeiterfassung aktiv ist

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

1. **Aufgabe starten**: 
   - Gib einen Aufgabennamen ein
   - Optional: Gib ein Projekt an
   - Klicke auf "Start"
2. **Zeit tracken**: Die Zeit wird automatisch in der Menüleiste angezeigt
   - Der Doppelpunkt blinkt, wenn eine Zeiterfassung aktiv ist
3. **Aufgabe beenden**: Klicke auf "Stop", um die Aufgabe zu beenden
4. **Verlauf ansehen**: Abgeschlossene Aufgaben werden im Verlauf angezeigt
   - Tasks zeigen Name, Projekt, Startzeit und Dauer
   - Klicke auf den ▶-Button, um einen Task erneut zu starten

## Datenspeicherung

Alle erfassten Aufgaben werden persistent in einer SQLite-Datenbank gespeichert:

- **Speicherort**: `~/.time-tracking/tasks.db`
- **Format**: SQLite 3
- **Automatisch**: Die Datenbank wird beim ersten Start automatisch erstellt
- **Persistenz**: Alle Aufgaben bleiben nach Anwendungsneustarts erhalten

Die letzten 100 Aufgaben werden in der Anwendung angezeigt, aber alle Daten bleiben in der Datenbank gespeichert.

## Technologie-Stack

- **Kotlin** 2.1.0
- **JetBrains Compose Multiplatform** 1.7.1 (Desktop)
  - _Nicht_ Android's androidx.compose, sondern JetBrains' plattformübergreifendes Compose
  - Optimiert für Desktop-Anwendungen (macOS, Windows, Linux)
- **Gradle** 8.11.1
- **Material 3** Design System
- **SQLite** 3.47.1.0 (über Xerial JDBC-Treiber)

## Lizenz

MIT
