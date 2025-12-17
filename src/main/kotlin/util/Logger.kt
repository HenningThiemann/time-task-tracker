package util

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

object Logger {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    private val logFile: File
    private var fileWriter: PrintWriter? = null

    init {
        // Erstelle Log-Verzeichnis im user home
        val logDir = File(System.getProperty("user.home"), "Library/Logs/TimeTaskTracker")
        logDir.mkdirs()

        logFile = File(logDir, "app.log")

        try {
            // Append-Modus, damit alte Logs nicht überschrieben werden
            fileWriter = PrintWriter(FileWriter(logFile, true), true)
            info("Logger", "=== TimeTaskTracker gestartet ===")
        } catch (e: Exception) {
            System.err.println("ERROR: Konnte Log-Datei nicht erstellen: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun log(level: String, tag: String, message: String, throwable: Throwable? = null) {
        val timestamp = dateFormat.format(Date())
        val logMessage = "[$timestamp] [$level] [$tag] $message"

        // In Console ausgeben
        println(logMessage)

        // In Datei schreiben
        try {
            fileWriter?.println(logMessage)
            throwable?.let {
                fileWriter?.println("Exception: ${it.message}")
                it.printStackTrace(fileWriter)
            }
        } catch (e: Exception) {
            System.err.println("ERROR: Konnte nicht in Log-Datei schreiben: ${e.message}")
        }
    }

    fun debug(tag: String, message: String) {
        log("DEBUG", tag, message)
    }

    fun info(tag: String, message: String) {
        log("INFO", tag, message)
    }

    fun warn(tag: String, message: String) {
        log("WARN", tag, message)
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        log("ERROR", tag, message, throwable)
    }

    fun close() {
        info("Logger", "=== TimeTaskTracker beendet ===")
        fileWriter?.close()
    }
}

