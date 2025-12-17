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
        // Create log directory in user home
        val logDir = File(System.getProperty("user.home"), "Library/Logs/TimeTaskTracker")
        logDir.mkdirs()

        logFile = File(logDir, "app.log")

        try {
            // Append mode, so old logs are not overwritten
            fileWriter = PrintWriter(FileWriter(logFile, true), true)
            info("Logger", "=== TimeTaskTracker started ===")
        } catch (e: Exception) {
            System.err.println("ERROR: Could not create log file: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun log(level: String, tag: String, message: String, throwable: Throwable? = null) {
        val timestamp = dateFormat.format(Date())
        val logMessage = "[$timestamp] [$level] [$tag] $message"

        // Print to console
        println(logMessage)

        // Write to file
        try {
            fileWriter?.println(logMessage)
            throwable?.let {
                fileWriter?.println("Exception: ${it.message}")
                it.printStackTrace(fileWriter)
            }
        } catch (e: Exception) {
            System.err.println("ERROR: Could not write to log file: ${e.message}")
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
        info("Logger", "=== TimeTaskTracker terminated ===")
        fileWriter?.close()
    }
}

