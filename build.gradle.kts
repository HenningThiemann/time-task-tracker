import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.1.0"
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
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "TimeTaskTracker"
            packageVersion = "1.0.0"
            
            macOS {
                iconFile.set(project.file("src/main/resources/icon.icns"))
                bundleID = "com.henningthiemann.timetasktracker"
                dockName = "Time Task Tracker"
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}
