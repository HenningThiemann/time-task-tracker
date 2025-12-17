import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Database {
    private val connection: Connection
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    init {
        // Erstelle Verzeichnis im Benutzerverzeichnis
        val homeDir = System.getProperty("user.home")
        val dbDir = Paths.get(homeDir, ".time-tracking")
        Files.createDirectories(dbDir)

        // Verbinde zur SQLite-Datenbank
        val dbPath = dbDir.resolve("tasks.db")
        connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")

        // Erstelle Tabelle falls nicht vorhanden
        createTableIfNotExists()
    }

    private fun createTableIfNotExists() {
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

    fun saveTask(task: CompletedTask) {
        val sql = """
            INSERT INTO completed_tasks (name, project_id, start_time, end_time, duration_seconds)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, task.name)

            // Wenn ein Projekt angegeben ist, hole/erstelle es und verwende die ID
            if (task.project != null) {
                val project = getOrCreateProject(task.project)
                stmt.setLong(2, project.id)
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER)
            }

            stmt.setString(3, task.startTime.format(dateTimeFormatter))
            stmt.setString(4, task.endTime.format(dateTimeFormatter))
            stmt.setLong(5, task.duration.seconds)
            stmt.executeUpdate()
        }
    }

    fun getAllTasks(): List<CompletedTask> {
        val sql = """
            SELECT t.name, p.name as project_name, t.start_time, t.end_time, t.duration_seconds
            FROM completed_tasks t
            LEFT JOIN projects p ON t.project_id = p.id
            ORDER BY t.start_time DESC
        """.trimIndent()

        val tasks = mutableListOf<CompletedTask>()

        connection.createStatement().use { stmt ->
            val rs: ResultSet = stmt.executeQuery(sql)
            while (rs.next()) {
                val name = rs.getString("name")
                val project = rs.getString("project_name")
                val startTime = LocalDateTime.parse(rs.getString("start_time"), dateTimeFormatter)
                val endTime = LocalDateTime.parse(rs.getString("end_time"), dateTimeFormatter)
                val durationSeconds = rs.getLong("duration_seconds")

                tasks.add(
                    CompletedTask(
                        name = name,
                        project = project,
                        startTime = startTime,
                        endTime = endTime,
                        duration = Duration.ofSeconds(durationSeconds)
                    )
                )
            }
        }

        return tasks
    }

    fun getRecentTasks(limit: Int): List<CompletedTask> {
        val sql = """
            SELECT t.name, p.name as project_name, t.start_time, t.end_time, t.duration_seconds
            FROM completed_tasks t
            LEFT JOIN projects p ON t.project_id = p.id
            ORDER BY t.start_time DESC
            LIMIT ?
        """.trimIndent()

        val tasks = mutableListOf<CompletedTask>()

        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, limit)
            val rs: ResultSet = stmt.executeQuery()
            while (rs.next()) {
                val name = rs.getString("name")
                val project = rs.getString("project_name")
                val startTime = LocalDateTime.parse(rs.getString("start_time"), dateTimeFormatter)
                val endTime = LocalDateTime.parse(rs.getString("end_time"), dateTimeFormatter)
                val durationSeconds = rs.getLong("duration_seconds")

                tasks.add(
                    CompletedTask(
                        name = name,
                        project = project,
                        startTime = startTime,
                        endTime = endTime,
                        duration = Duration.ofSeconds(durationSeconds)
                    )
                )
            }
        }

        return tasks
    }

    fun close() {
        connection.close()
    }

    // === Task-Verwaltung (Start/Stop) ===

    fun startTask(name: String, projectName: String?): Long {
        val sql = """
            INSERT INTO completed_tasks (name, project_id, start_time, duration_seconds, is_completed)
            VALUES (?, ?, ?, 0, 0)
        """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, name)

            if (projectName != null) {
                val project = getOrCreateProject(projectName)
                stmt.setLong(2, project.id)
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER)
            }

            stmt.setString(3, LocalDateTime.now().format(dateTimeFormatter))
            stmt.executeUpdate()

            // Hole die generierte ID
            val rs = stmt.generatedKeys
            if (rs.next()) {
                return rs.getLong(1)
            }
            throw SQLException("Failed to start task, no ID obtained")
        }
    }

    fun stopTask(taskId: Long, additionalDuration: Duration) {
        val sql = """
            UPDATE completed_tasks
            SET end_time = ?,
                duration_seconds = duration_seconds + ?
            WHERE id = ?
        """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, LocalDateTime.now().format(dateTimeFormatter))
            stmt.setLong(2, additionalDuration.seconds)
            stmt.setLong(3, taskId)
            stmt.executeUpdate()
        }
    }

    fun updateTaskDuration(taskId: Long, additionalDuration: Duration) {
        val sql = """
            UPDATE completed_tasks
            SET duration_seconds = duration_seconds + ?
            WHERE id = ?
        """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            stmt.setLong(1, additionalDuration.seconds)
            stmt.setLong(2, taskId)
            stmt.executeUpdate()
        }
    }

    fun getRunningTask(): RunningTask? {
        val sql = """
            SELECT t.id, t.name, p.name as project_name, t.start_time, t.duration_seconds
            FROM completed_tasks t
            LEFT JOIN projects p ON t.project_id = p.id
            WHERE t.is_completed = 0
            ORDER BY t.start_time DESC
            LIMIT 1
        """.trimIndent()

        connection.createStatement().use { stmt ->
            val rs: ResultSet = stmt.executeQuery(sql)
            if (rs.next()) {
                return RunningTask(
                    id = rs.getLong("id"),
                    name = rs.getString("name"),
                    project = rs.getString("project_name"),
                    startTime = LocalDateTime.parse(rs.getString("start_time"), dateTimeFormatter),
                    accumulatedDuration = Duration.ofSeconds(rs.getLong("duration_seconds"))
                )
            }
        }
        return null
    }

    fun findRunningTaskByNameAndProject(name: String, projectName: String?): RunningTask? {
        val sql = if (projectName == null) {
            """
            SELECT t.id, t.name, p.name as project_name, t.start_time, t.duration_seconds
            FROM completed_tasks t
            LEFT JOIN projects p ON t.project_id = p.id
            WHERE t.is_completed = 0 AND t.name = ? AND t.project_id IS NULL
            LIMIT 1
            """.trimIndent()
        } else {
            """
            SELECT t.id, t.name, p.name as project_name, t.start_time, t.duration_seconds
            FROM completed_tasks t
            LEFT JOIN projects p ON t.project_id = p.id
            WHERE t.is_completed = 0 AND t.name = ? AND p.name = ?
            LIMIT 1
            """.trimIndent()
        }

        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, name)
            if (projectName != null) {
                stmt.setString(2, projectName)
            }

            val rs: ResultSet = stmt.executeQuery()
            if (rs.next()) {
                return RunningTask(
                    id = rs.getLong("id"),
                    name = rs.getString("name"),
                    project = rs.getString("project_name"),
                    startTime = LocalDateTime.parse(rs.getString("start_time"), dateTimeFormatter),
                    accumulatedDuration = Duration.ofSeconds(rs.getLong("duration_seconds"))
                )
            }
        }
        return null
    }

    // === Projekt-Verwaltung ===

    fun createProject(name: String, color: String? = null): Long {
        val sql = """
            INSERT INTO projects (name, color)
            VALUES (?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, name)
            stmt.setString(2, color)
            stmt.executeUpdate()

            // Hole die generierte ID
            val rs = stmt.generatedKeys
            if (rs.next()) {
                return rs.getLong(1)
            }
            throw SQLException("Failed to create project, no ID obtained")
        }
    }

    fun getAllProjects(): List<Project> {
        val sql = """
            SELECT id, name, color, created_at
            FROM projects
            ORDER BY name ASC
        """.trimIndent()

        val projects = mutableListOf<Project>()

        connection.createStatement().use { stmt ->
            val rs: ResultSet = stmt.executeQuery(sql)
            while (rs.next()) {
                projects.add(
                    Project(
                        id = rs.getLong("id"),
                        name = rs.getString("name"),
                        color = rs.getString("color")
                    )
                )
            }
        }

        return projects
    }

    fun getProjectById(id: Long): Project? {
        val sql = """
            SELECT id, name, color
            FROM projects
            WHERE id = ?
        """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            stmt.setLong(1, id)
            val rs: ResultSet = stmt.executeQuery()
            if (rs.next()) {
                return Project(
                    id = rs.getLong("id"),
                    name = rs.getString("name"),
                    color = rs.getString("color")
                )
            }
        }
        return null
    }

    fun getProjectByName(name: String): Project? {
        val sql = """
            SELECT id, name, color
            FROM projects
            WHERE name = ?
        """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, name)
            val rs: ResultSet = stmt.executeQuery()
            if (rs.next()) {
                return Project(
                    id = rs.getLong("id"),
                    name = rs.getString("name"),
                    color = rs.getString("color")
                )
            }
        }
        return null
    }

    fun updateProject(id: Long, name: String, color: String? = null) {
        val sql = """
            UPDATE projects
            SET name = ?, color = ?
            WHERE id = ?
        """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, name)
            stmt.setString(2, color)
            stmt.setLong(3, id)
            stmt.executeUpdate()
        }
    }

    fun deleteProject(id: Long) {
        val sql = """
            DELETE FROM projects
            WHERE id = ?
        """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate()
        }
    }

    fun getOrCreateProject(name: String): Project {
        return getProjectByName(name) ?: run {
            val id = createProject(name)
            Project(id, name, null)
        }
    }
}
