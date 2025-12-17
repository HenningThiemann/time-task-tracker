package manager

import androidx.compose.runtime.mutableStateOf
import config.Config
import database.DatabaseConnection
import database.ProjectRepository
import database.TaskRepository
import model.CompletedTask
import model.Project
import java.time.Duration
import java.time.LocalDateTime

class TaskManager {
    val currentTask = mutableStateOf<String?>(null)
    val currentProject = mutableStateOf<String?>(null)
    val elapsedTime = mutableStateOf(Duration.ZERO)
    val taskHistory = mutableStateOf<List<CompletedTask>>(emptyList())
    val projects = mutableStateOf<List<Project>>(emptyList())
    val isPaused = mutableStateOf(false)

    private var runningTaskId: Long? = null
    private var sessionStartTime: LocalDateTime? = null
    private var accumulatedDuration: Duration = Duration.ZERO
    private var lastSaveTime: LocalDateTime? = null

    private val dbConnection: DatabaseConnection = DatabaseConnection()
    private val projectRepository: ProjectRepository = ProjectRepository(dbConnection.connection)
    private val taskRepository: TaskRepository = TaskRepository(dbConnection.connection, projectRepository)

    init {
        loadTasksFromDatabase()
        loadProjectsFromDatabase()
        loadRunningTask()
    }

    private fun loadRunningTask() {
        try {
            val runningTask = taskRepository.getRunningTask()
            if (runningTask != null) {
                // Task is loaded, but NOT automatically started
                // The user must manually resume the task
                currentTask.value = runningTask.name
                currentProject.value = runningTask.project
                runningTaskId = runningTask.id
                // sessionStartTime remains null -> task is paused
                sessionStartTime = null
                accumulatedDuration = runningTask.accumulatedDuration
                elapsedTime.value = accumulatedDuration
                isPaused.value = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadTasksFromDatabase() {
        try {
            // Load all tasks for project overview, not just the recent ones
            val tasks = taskRepository.getAllTasks()
            taskHistory.value = tasks
        } catch (e: Exception) {
            e.printStackTrace()
            taskHistory.value = emptyList()
        }
    }

    private fun loadProjectsFromDatabase() {
        try {
            projects.value = projectRepository.getAllProjects()
        } catch (e: Exception) {
            e.printStackTrace()
            projects.value = emptyList()
        }
    }

    // Project Management
    fun createProject(name: String, color: String? = null) {
        try {
            projectRepository.createProject(name, color)
            loadProjectsFromDatabase()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateProject(id: Long, name: String, color: String? = null) {
        try {
            projectRepository.updateProject(id, name, color)
            loadProjectsFromDatabase()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteProject(id: Long) {
        try {
            projectRepository.deleteProject(id)
            loadProjectsFromDatabase()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Task Management
    fun deleteTask(task: CompletedTask) {
        try {
            taskRepository.deleteTask(task)
            loadTasksFromDatabase()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startTask(taskName: String, projectName: String? = null) {
        try {
            val taskId = taskRepository.startTask(taskName, projectName)

            currentTask.value = taskName
            currentProject.value = projectName
            runningTaskId = taskId
            sessionStartTime = LocalDateTime.now()
            accumulatedDuration = Duration.ZERO
            elapsedTime.value = Duration.ZERO
            lastSaveTime = LocalDateTime.now()
            isPaused.value = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resumeTask() {
        // Resumes a paused task
        if (runningTaskId != null && sessionStartTime == null) {
            sessionStartTime = LocalDateTime.now()
            lastSaveTime = LocalDateTime.now()
            isPaused.value = false
        }
    }

    fun restartTask(task: CompletedTask) {
        try {
            val existingTask = taskRepository.findRunningTaskByNameAndProject(task.name, task.project)

            if (existingTask != null) {
                // Task is already running - resume it as current task and start timer
                currentTask.value = existingTask.name
                currentProject.value = existingTask.project
                runningTaskId = existingTask.id
                sessionStartTime = LocalDateTime.now()
                accumulatedDuration = existingTask.accumulatedDuration
                elapsedTime.value = accumulatedDuration
                lastSaveTime = LocalDateTime.now()
                isPaused.value = false
            } else {
                // Task is not running yet - create a new one
                if (currentTask.value != null) {
                    stopTask()
                }
                startTask(task.name, task.project)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopTask() {
        val taskId = runningTaskId
        val sessionStart = sessionStartTime

        if (taskId != null && sessionStart != null) {
            try {
                val now = LocalDateTime.now()
                val additionalDuration = Duration.between(sessionStart, now)

                taskRepository.stopTask(taskId, additionalDuration)
                loadTasksFromDatabase()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (taskId != null) {
            // Task is paused, stop it without additional duration
            try {
                taskRepository.stopTask(taskId, Duration.ZERO)
                loadTasksFromDatabase()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        currentTask.value = null
        currentProject.value = null
        runningTaskId = null
        sessionStartTime = null
        accumulatedDuration = Duration.ZERO
        elapsedTime.value = Duration.ZERO
        lastSaveTime = null
        isPaused.value = false
    }

    fun updateElapsedTime() {
        val sessionStart = sessionStartTime
        val taskId = runningTaskId

        if (sessionStart != null && taskId != null) {
            val now = LocalDateTime.now()
            val sessionDuration = Duration.between(sessionStart, now)
            elapsedTime.value = accumulatedDuration.plus(sessionDuration)

            val lastSave = lastSaveTime
            if (lastSave == null || Duration.between(lastSave, now).seconds >= Config.SAVE_INTERVAL_SECONDS) {
                try {
                    taskRepository.updateTaskDuration(taskId, sessionDuration)

                    accumulatedDuration = accumulatedDuration.plus(sessionDuration)
                    sessionStartTime = now
                    lastSaveTime = now
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun close() {
        if (runningTaskId != null) {
            val sessionStart = sessionStartTime
            if (sessionStart != null) {
                try {
                    val now = LocalDateTime.now()
                    val additionalDuration = Duration.between(sessionStart, now)
                    taskRepository.updateTaskDuration(runningTaskId!!, additionalDuration)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        dbConnection.close()
    }
}
