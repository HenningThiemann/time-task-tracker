import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import config.Strings
import kotlinx.coroutines.delay
import manager.TaskManager
import ui.tabs.ProjectsTab
import ui.tabs.TasksTab
import ui.theme.ThemeManager
import util.buildTooltipText
import util.createTrayIcon
import java.time.Duration

fun main() = application {
    var isVisible by remember { mutableStateOf(false) }
    val taskManager = remember { TaskManager() }
    var trayIcon by remember { mutableStateOf(createTrayIcon(null, Duration.ZERO, false)) }
    var tooltipText by remember { mutableStateOf(Strings.APP_TITLE) }
    var showColon by remember { mutableStateOf(true) }

    // Schließe Datenbankverbindung beim Beenden
    DisposableEffect(Unit) {
        onDispose {
            taskManager.close()
        }
    }

    // Update menu bar every second
    LaunchedEffect(Unit) {
        while (true) {
            taskManager.updateElapsedTime()

            // Toggle colon visibility every second when task is active
            if (taskManager.currentTask.value != null) {
                showColon = !showColon
            } else {
                showColon = true
            }

            // Update tray icon every second to show current time
            trayIcon = createTrayIcon(
                taskManager.currentTask.value,
                taskManager.elapsedTime.value,
                showColon
            )

            // Update tooltip text every second
            tooltipText = buildTooltipText(taskManager.currentTask.value, taskManager.elapsedTime.value)

            delay(1000)
        }
    }

    // Menu bar tray icon
    Tray(
        icon = BitmapPainter(trayIcon.toComposeImageBitmap()),
        tooltip = tooltipText,
        menu = {
            Item(Strings.MENU_SHOW_WINDOW) {
                isVisible = true
            }
            Separator()
            Item(Strings.MENU_QUIT) {
                exitApplication()
            }
        },
        onAction = {
            isVisible = true
        }
    )

    if (isVisible) {
        Window(
            onCloseRequest = { isVisible = false },
            title = Strings.APP_TITLE,
            state = rememberWindowState(width = 1200.dp, height = 800.dp)
        ) {
            val isDarkMode by ThemeManager.isDarkMode

            MaterialTheme(
                colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()
            ) {
                TimeTaskTrackerApp(taskManager)
            }
        }
    }
}

@Composable
fun TimeTaskTrackerApp(taskManager: TaskManager) {
    val currentTask by taskManager.currentTask
    val currentProject by taskManager.currentProject
    val elapsedTime by taskManager.elapsedTime
    val taskHistory by taskManager.taskHistory
    val projects by taskManager.projects
    val isPaused by taskManager.isPaused
    val isDarkMode by ThemeManager.isDarkMode

    var taskName by remember { mutableStateOf("") }
    var projectName by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableStateOf(0) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header mit Tabs und Dark Mode Toggle
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title und Dark Mode Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(48.dp)) // Platzhalter für Symmetrie

                    Text(
                        text = Strings.APP_TITLE,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = { ThemeManager.toggleTheme() }
                    ) {
                        Text(
                            text = if (isDarkMode) "☀️" else "🌙",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text(Strings.TAB_TASKS) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text(Strings.TAB_PROJECTS) }
                    )
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> TasksTab(
                    taskManager = taskManager,
                    currentTask = currentTask,
                    currentProject = currentProject,
                    elapsedTime = elapsedTime,
                    taskHistory = taskHistory,
                    projects = projects,
                    isPaused = isPaused,
                    taskName = taskName,
                    projectName = projectName,
                    onTaskNameChange = { taskName = it },
                    onProjectNameChange = { projectName = it },
                    onStartTask = {
                        taskManager.startTask(taskName, projectName.ifBlank { null })
                        taskName = ""
                        projectName = ""
                    }
                )
                1 -> ProjectsTab(
                    taskManager = taskManager,
                    projects = projects,
                    allTasks = taskHistory,
                    onCreateProject = { name -> taskManager.createProject(name) },
                    onDeleteProject = { id -> taskManager.deleteProject(id) },
                    onTaskResumed = { selectedTabIndex = 0 }
                )
            }
        }
    }
}

