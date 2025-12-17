import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.1.0"
    // JetBrains Compose Multiplatform (Desktop) - not Android's androidx.compose
    id("org.jetbrains.compose") version "1.7.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
}

group = "com.henningthiemann"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // JetBrains Compose Multiplatform Desktop Dependency
    // This brings androidx.compose.* packages from JetBrains, not Android
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)

    // SQLite Database
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "TimeTaskTracker"
            packageVersion = "1.0.0"
            
            // Include necessary JVM modules for SQL support
            modules("java.sql")

            macOS {
                bundleID = "com.henningthiemann.timetasktracker"
                iconFile.set(project.file("src/main/resources/app-icon.icns"))
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}
