import database.DatabaseConnection
import database.ProjectRepository
import database.TaskRepository
import model.CompletedTask
import model.Project
import model.RunningTask
import java.time.Duration

/**
 * Facade class for database operations.
 * Delegates to specialized repositories for better maintainability.
 */
class Database {
    private val dbConnection: DatabaseConnection = DatabaseConnection()
    private val projectRepository: ProjectRepository = ProjectRepository(dbConnection.connection)
    private val taskRepository: TaskRepository = TaskRepository(dbConnection.connection, projectRepository)

    // === Task Management ===

    fun saveTask(task: CompletedTask) = taskRepository.saveTask(task)

    fun getAllTasks(): List<CompletedTask> = taskRepository.getAllTasks()

    fun getRecentTasks(limit: Int): List<CompletedTask> = taskRepository.getRecentTasks(limit)

    fun startTask(name: String, projectName: String?): Long = taskRepository.startTask(name, projectName)

    fun stopTask(taskId: Long, additionalDuration: Duration) = taskRepository.stopTask(taskId, additionalDuration)

    fun updateTaskDuration(taskId: Long, additionalDuration: Duration) =
        taskRepository.updateTaskDuration(taskId, additionalDuration)

    fun getRunningTask(): RunningTask? = taskRepository.getRunningTask()

    fun findRunningTaskByNameAndProject(name: String, projectName: String?): RunningTask? =
        taskRepository.findRunningTaskByNameAndProject(name, projectName)

    // === Project Management ===

    fun createProject(name: String, color: String? = null): Long = projectRepository.createProject(name, color)

    fun getAllProjects(): List<Project> = projectRepository.getAllProjects()

    fun getProjectById(id: Long): Project? = projectRepository.getProjectById(id)

    fun getProjectByName(name: String): Project? = projectRepository.getProjectByName(name)

    fun updateProject(id: Long, name: String, color: String? = null) =
        projectRepository.updateProject(id, name, color)

    fun deleteProject(id: Long) = projectRepository.deleteProject(id)

    fun getOrCreateProject(name: String): Project = projectRepository.getOrCreateProject(name)

    // === Connection Management ===

    fun close() {
        dbConnection.close()
    }
}
