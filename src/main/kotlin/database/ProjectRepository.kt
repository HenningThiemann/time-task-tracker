package database

import model.Project
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

class ProjectRepository(private val connection: Connection) {

    fun createProject(name: String, color: String? = null): Long {
        val sql = """
            INSERT INTO projects (name, color)
            VALUES (?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, name)
            stmt.setString(2, color)
            stmt.executeUpdate()

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

