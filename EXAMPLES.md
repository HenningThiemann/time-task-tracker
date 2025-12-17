# Time Task Tracker - Beispielkonfigurationen

## Anwendungsstart-Beispiele

### 1. Standard-Start
```bash
./gradlew run
```

### 2. Mit speziellen JVM-Optionen
```bash
./gradlew run -Dorg.gradle.jvmargs="-Xmx4096m"
```

### 3. Debug-Modus
```bash
./gradlew run --debug-jvm
```

## Build-Varianten

### Debug-Build
```bash
./gradlew build
```

### Release-Build
```bash
./gradlew packageReleaseDmg
```

### Uber-JAR erstellen
```bash
./gradlew packageUberJarForCurrentOS
```

## Anpassungsmöglichkeiten

### App-Name ändern

In `build.gradle.kts`:
```kotlin
compose.desktop {
    application {
        nativeDistributions {
            packageName = "MeinTaskTracker"  // Ändere hier den Namen
        }
    }
}
```

### Bundle-ID ändern

In `build.gradle.kts`:
```kotlin
macOS {
    bundleID = "com.meinefirma.tasktracker"  // Ändere hier die Bundle-ID
}
```

### Version aktualisieren

In `build.gradle.kts`:
```kotlin
version = "2.0.0"  // Ändere hier die Version
```

## Systemanforderungen

### Minimum
- macOS 10.13 oder höher
- Java JDK 17
- 2 GB RAM
- 100 MB Festplattenspeicher

### Empfohlen
- macOS 12 oder höher
- Java JDK 17 oder höher
- 4 GB RAM
- 200 MB Festplattenspeicher

## Fehlerbehebung

### Problem: Gradle wrapper funktioniert nicht
```bash
# Lösung: Wrapper neu generieren
gradle wrapper --gradle-version 8.11.1
```

### Problem: Zu wenig Speicher beim Build
```bash
# Lösung: Mehr Speicher in gradle.properties zuweisen
org.gradle.jvmargs=-Xmx4096M
```

### Problem: DMG-Erstellung schlägt fehl
- Stelle sicher, dass du auf macOS bist
- Überprüfe, dass alle Abhängigkeiten installiert sind
- Führe `./gradlew clean` vor dem erneuten Versuch aus

## Continuous Integration

Beispiel für GitHub Actions:

```yaml
name: Build

on: [push, pull_request]

jobs:
  build:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build
        run: ./gradlew build
      - name: Package DMG
        run: ./gradlew packageDmg
      - name: Upload DMG
        uses: actions/upload-artifact@v3
        with:
          name: TimeTaskTracker-DMG
          path: build/compose/binaries/main/dmg/*.dmg
```

## Lizenzierung

Dieses Projekt steht unter der MIT-Lizenz. Siehe LICENSE-Datei für Details.
