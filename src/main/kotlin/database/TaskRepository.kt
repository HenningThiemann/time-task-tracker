package database

import model.CompletedTask
import model.RunningTask
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TaskRepository(
    private val connection: Connection,
    private val projectRepository: ProjectRepository
) {
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun saveTask(task: CompletedTask) {
        val sql = """
            INSERT INTO completed_tasks (name, project_id, start_time, end_time, duration_seconds)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, task.name)

            if (task.project != null) {
                val project = projectRepository.getOrCreateProject(task.project)
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

    fun deleteTask(task: CompletedTask) {
        val sql = """
            DELETE FROM completed_tasks
            WHERE name = ? AND start_time = ? AND end_time = ?
        """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, task.name)
            stmt.setString(2, task.startTime.format(dateTimeFormatter))
            stmt.setString(3, task.endTime.format(dateTimeFormatter))
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
                tasks.add(buildCompletedTask(rs))
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
                tasks.add(buildCompletedTask(rs))
            }
        }

        return tasks
    }

    fun startTask(name: String, projectName: String?): Long {
        val sql = """
            INSERT INTO completed_tasks (name, project_id, start_time, duration_seconds, is_completed)
            VALUES (?, ?, ?, 0, 0)
        """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, name)

            if (projectName != null) {
                val project = projectRepository.getOrCreateProject(projectName)
                stmt.setLong(2, project.id)
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER)
            }

            stmt.setString(3, LocalDateTime.now().format(dateTimeFormatter))
            stmt.executeUpdate()

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
                return buildRunningTask(rs)
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
                return buildRunningTask(rs)
            }
        }
        return null
    }

    private fun buildCompletedTask(rs: ResultSet): CompletedTask {
        return CompletedTask(
            name = rs.getString("name"),
            project = rs.getString("project_name"),
            startTime = LocalDateTime.parse(rs.getString("start_time"), dateTimeFormatter),
            endTime = LocalDateTime.parse(rs.getString("end_time"), dateTimeFormatter),
            duration = Duration.ofSeconds(rs.getLong("duration_seconds"))
        )
    }

    private fun buildRunningTask(rs: ResultSet): RunningTask {
        return RunningTask(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            project = rs.getString("project_name"),
            startTime = LocalDateTime.parse(rs.getString("start_time"), dateTimeFormatter),
            accumulatedDuration = Duration.ofSeconds(rs.getLong("duration_seconds"))
        )
    }
}
