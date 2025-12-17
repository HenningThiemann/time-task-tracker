package database

import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager

class DatabaseConnection {
    val connection: Connection

    init {
        // Erstelle Verzeichnis im Benutzerverzeichnis
        val homeDir = System.getProperty("user.home")
        val dbDir = Paths.get(homeDir, ".time-tracking")
        Files.createDirectories(dbDir)

        // Verbinde zur SQLite-Datenbank
        val dbPath = dbDir.resolve("tasks.db")
        connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")

        // Erstelle Tabellen falls nicht vorhanden
        createTablesIfNotExists()
    }

    private fun createTablesIfNotExists() {
        // Projekte-Tabelle
        val projectsTableSql = """
            CREATE TABLE IF NOT EXISTS projects (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                color TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

        // Tasks-Tabelle mit Foreign Key zu Projekten
        val tasksTableSql = """
            CREATE TABLE IF NOT EXISTS completed_tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                project_id INTEGER,
                start_time TEXT NOT NULL,
                end_time TEXT,
                duration_seconds INTEGER NOT NULL DEFAULT 0,
                is_completed BOOLEAN NOT NULL DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL
            )
        """.trimIndent()

        connection.createStatement().execute(projectsTableSql)
        connection.createStatement().execute(tasksTableSql)
    }

    fun close() {
        connection.close()
    }
}

