# Entwicklerdokumentation

## Projektstruktur

```
time-task-tracker/
├── build.gradle.kts          # Hauptbuild-Datei
├── settings.gradle.kts        # Gradle-Einstellungen
├── gradle.properties          # Gradle-Eigenschaften
├── src/
│   └── main/
│       ├── kotlin/
│       │   └── Main.kt        # Hauptanwendung
│       └── resources/         # Ressourcen (Icons, etc.)
└── README.md
```

## Architektur

### Hauptkomponenten

1. **TaskManager**: Verwaltet den aktuellen Task und die Historie
   - `startTask()`: Startet einen neuen Task
   - `stopTask()`: Beendet den aktuellen Task
   - `updateElapsedTime()`: Aktualisiert die verstrichene Zeit

2. **TimeTaskTrackerApp**: Hauptbenutzeroberfläche
   - Zeigt aktuelle Task-Information
   - Ermöglicht das Starten/Stoppen von Tasks
   - Zeigt die Task-Historie

3. **Tray Integration**: macOS Menüleisten-Integration
   - Zeigt ein dynamisches Icon mit der aktuellen Zeit
   - Bietet Schnellzugriff auf die Anwendung

## Verwendete Technologien

- **Kotlin 2.1.0**: Moderne JVM-Sprache
- **JetBrains Compose Multiplatform 1.7.1**: Deklaratives UI-Framework
  - Plattformübergreifendes Framework von JetBrains (nicht Android's androidx.compose)
  - Desktop-optimiert für macOS, Windows und Linux
  - Die `androidx.compose.*` Imports stammen aus JetBrains Compose Desktop, nicht aus dem Android SDK
- **Material 3**: Modernes Design-System
- **Gradle 8.11.1**: Build-Automatisierung

## Entwicklungsworkflow

### Lokale Entwicklung

```bash
# Projekt kompilieren
./gradlew build

# Anwendung ausführen
./gradlew run

# Tests ausführen
./gradlew test

# Code aufräumen
./gradlew clean
```

### Distribution

```bash
# DMG für macOS erstellen
./gradlew packageDmg

# Alle Distributions-Formate erstellen
./gradlew packageDistributionForCurrentOS
```

### Debugging

Für Debugging mit IntelliJ IDEA:
1. Öffne das Projekt in IntelliJ IDEA
2. Warte bis Gradle synchronisiert ist
3. Führe die `Main.kt` aus oder nutze die Gradle-Tasks

## UI-Anpassungen

Die UI verwendet Material 3 Design und kann durch Änderung der Farben in `MaterialTheme` angepasst werden.

### Beispiel für Farbanpassung

```kotlin
MaterialTheme(
    colorScheme = darkColorScheme(
        primary = Color(0xFF6200EE),
        secondary = Color(0xFF03DAC6)
    )
) {
    TimeTaskTrackerApp(taskManager)
}
```

## Performance

- Die Zeit wird jede Sekunde aktualisiert
- Das Tray-Icon wird bei jedem Update neu generiert
- Die Task-Historie ist auf die letzten 10 Einträge im UI begrenzt

## Zukünftige Erweiterungen

Mögliche Verbesserungen:
- Persistente Speicherung der Task-Historie
- Kategorien für Tasks
- Berichte und Statistiken
- Export-Funktionalität
- Keyboard-Shortcuts
- Multi-Task-Unterstützung mit Pausenfunktion
