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
import kotlinx.coroutines.delay
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// UI Text Constants - können später in Ressourcen-Dateien ausgelagert werden
private object Strings {
    const val APP_TITLE = "Time Task Tracker"
    const val MENU_SHOW_WINDOW = "Fenster anzeigen"
    const val MENU_QUIT = "Beenden"
    const val CURRENT_TASK = "Aktuelle Aufgabe"
    const val NO_ACTIVE_TASK = "Keine aktive Aufgabe"
    const val TASK_NAME_LABEL = "Aufgabenname"
    const val PROJECT_LABEL = "Projekt (optional)"
    const val BUTTON_START = "Start"
    const val BUTTON_STOP = "Stop"
    const val HISTORY_TITLE = "Verlauf"
    const val TAB_TASKS = "Aufgaben"
    const val TAB_PROJECTS = "Projekte"
    const val PROJECTS_TITLE = "Projekt-Verwaltung"
    const val NEW_PROJECT_NAME = "Neuer Projektname"
    const val BUTTON_ADD_PROJECT = "Projekt hinzufügen"
    const val NO_PROJECTS = "Keine Projekte vorhanden"
    const val PROJECT_NAME_LABEL = "Projektname"
    const val BUTTON_CREATE_PROJECT = "Projekt erstellen"
    const val BUTTON_DELETE = "Löschen"
}

private object Config {
    const val MAX_HISTORY_ITEMS_DISPLAYED = 10
}

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
            state = rememberWindowState(width = 400.dp, height = 600.dp)
        ) {
            MaterialTheme {
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
            // Header mit Tabs
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Strings.APP_TITLE,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

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
                    projects = projects,
                    onCreateProject = { name -> taskManager.createProject(name) },
                    onDeleteProject = { id -> taskManager.deleteProject(id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksTab(
    taskManager: TaskManager,
    currentTask: String?,
    currentProject: String?,
    elapsedTime: Duration,
    taskHistory: List<CompletedTask>,
    projects: List<Project>,
    taskName: String,
    projectName: String,
    onTaskNameChange: (String) -> Unit,
    onProjectNameChange: (String) -> Unit,
    onStartTask: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Current Task Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (currentTask != null)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (currentTask != null) Strings.CURRENT_TASK else Strings.NO_ACTIVE_TASK,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                currentTask?.let { task ->
                    Text(
                        text = task,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    currentProject?.let { project ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = project,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = formatDuration(elapsedTime),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } ?: run {
                    Text(
                        text = "--:--:--",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Task Input and Controls
        OutlinedTextField(
            value = taskName,
            onValueChange = onTaskNameChange,
            label = { Text(Strings.TASK_NAME_LABEL) },
            modifier = Modifier.fillMaxWidth(),
            enabled = currentTask == null,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Projekt-Auswahl
        var expandedProjectDropdown by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expandedProjectDropdown,
            onExpandedChange = { expandedProjectDropdown = !expandedProjectDropdown && currentTask == null }
        ) {
            OutlinedTextField(
                value = projectName,
                onValueChange = onProjectNameChange,
                label = { Text(Strings.PROJECT_LABEL) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                enabled = currentTask == null,
                singleLine = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProjectDropdown)
                }
            )

            ExposedDropdownMenu(
                expanded = expandedProjectDropdown,
                onDismissRequest = { expandedProjectDropdown = false }
            ) {
                projects.forEach { project ->
                    DropdownMenuItem(
                        text = { Text(project.name) },
                        onClick = {
                            onProjectNameChange(project.name)
                            expandedProjectDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onStartTask,
                modifier = Modifier.weight(1f),
                enabled = currentTask == null && taskName.isNotBlank()
            ) {
                Text(Strings.BUTTON_START)
            }

            Button(
                onClick = { taskManager.stopTask() },
                modifier = Modifier.weight(1f),
                enabled = currentTask != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(Strings.BUTTON_STOP)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Task History
        if (taskHistory.isNotEmpty()) {
            Text(
                text = Strings.HISTORY_TITLE,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    taskHistory.reversed().take(Config.MAX_HISTORY_ITEMS_DISPLAYED).forEach { task ->
                        TaskHistoryItem(
                            task = task,
                            onRestart = { completedTask ->
                                if (currentTask == null) {
                                    taskManager.restartTask(completedTask)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectsTab(
    projects: List<Project>,
    onCreateProject: (String) -> Unit,
    onDeleteProject: (Long) -> Unit
) {
    var newProjectName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = Strings.PROJECTS_TITLE,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Neues Projekt erstellen
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newProjectName,
                onValueChange = { newProjectName = it },
                label = { Text(Strings.NEW_PROJECT_NAME) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Button(
                onClick = {
                    if (newProjectName.isNotBlank()) {
                        onCreateProject(newProjectName)
                        newProjectName = ""
                    }
                },
                enabled = newProjectName.isNotBlank()
            ) {
                Text(Strings.BUTTON_ADD_PROJECT)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Projekt-Liste
        if (projects.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = Strings.NO_PROJECTS,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    projects.forEach { project ->
                        ProjectItem(
                            project = project,
                            onDelete = { onDeleteProject(project.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectItem(project: Project, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onDelete) {
                Text(
                    text = "🗑",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun TaskHistoryItem(task: CompletedTask, onRestart: (CompletedTask) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                task.project?.let { project ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = project,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = task.startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDuration(task.duration),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { onRestart(task) }
            ) {
                Text(
                    text = "▶",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

class TaskManager {
    val currentTask = mutableStateOf<String?>(null)
    val currentProject = mutableStateOf<String?>(null)
    val elapsedTime = mutableStateOf(Duration.ZERO)
    val taskHistory = mutableStateOf<List<CompletedTask>>(emptyList())
    val projects = mutableStateOf<List<Project>>(emptyList())

    private var startTime: LocalDateTime? = null
    private val database: Database = Database()

    companion object {
        private const val MAX_HISTORY_SIZE = 100 // Keep last 100 tasks to prevent unbounded growth
    }

    init {
        // Lade gespeicherte Tasks aus der Datenbank
        loadTasksFromDatabase()
        loadProjectsFromDatabase()
    }

    private fun loadTasksFromDatabase() {
        try {
            val tasks = database.getRecentTasks(MAX_HISTORY_SIZE)
            taskHistory.value = tasks
        } catch (e: Exception) {
            e.printStackTrace()
            // Falls Fehler beim Laden, verwende leere Liste
            taskHistory.value = emptyList()
        }
    }

    private fun loadProjectsFromDatabase() {
        try {
            projects.value = database.getAllProjects()
        } catch (e: Exception) {
            e.printStackTrace()
            projects.value = emptyList()
        }
    }

    // Projekt-Verwaltung
    fun createProject(name: String, color: String? = null) {
        try {
            database.createProject(name, color)
            loadProjectsFromDatabase()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateProject(id: Long, name: String, color: String? = null) {
        try {
            database.updateProject(id, name, color)
            loadProjectsFromDatabase()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteProject(id: Long) {
        try {
            database.deleteProject(id)
            loadProjectsFromDatabase()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startTask(taskName: String, projectName: String? = null) {
        currentTask.value = taskName
        currentProject.value = projectName
        startTime = LocalDateTime.now()
        elapsedTime.value = Duration.ZERO
    }

    fun restartTask(task: CompletedTask) {
        startTask(task.name, task.project)
    }

    fun stopTask() {
        val task = currentTask.value
        val project = currentProject.value
        val start = startTime

        if (task != null && start != null) {
            val endTime = LocalDateTime.now()
            val duration = Duration.between(start, endTime)

            val completedTask = CompletedTask(
                name = task,
                project = project,
                startTime = start,
                endTime = endTime,
                duration = duration
            )

            // Speichere Task in der Datenbank
            try {
                database.saveTask(completedTask)
                // Aktualisiere die History aus der Datenbank
                loadTasksFromDatabase()
            } catch (e: Exception) {
                e.printStackTrace()
                // Falls DB-Fehler, füge nur zur In-Memory-Liste hinzu
                val newHistory = (taskHistory.value + completedTask).takeLast(MAX_HISTORY_SIZE)
                taskHistory.value = newHistory
            }
        }

        currentTask.value = null
        currentProject.value = null
        startTime = null
        elapsedTime.value = Duration.ZERO
    }

    fun updateElapsedTime() {
        startTime?.let {
            elapsedTime.value = Duration.between(it, LocalDateTime.now())
        }
    }

    fun close() {
        database.close()
    }
}

data class CompletedTask(
    val name: String,
    val project: String? = null,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val duration: Duration
)

data class Project(
    val id: Long,
    val name: String,
    val color: String? = null
)

fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutesPart()
    val seconds = duration.toSecondsPart()
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

// Kompaktes Format für macOS Tray (nur HH:MM ohne Sekunden)
fun formatDurationForTray(duration: Duration, showColon: Boolean): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutesPart()
    val separator = if (showColon) ":" else " "
    return String.format("%d%s%02d", hours, separator, minutes)
}

fun buildTooltipText(currentTask: String?, elapsedTime: Duration): String {
    return if (currentTask != null) {
        "$currentTask - ${formatDuration(elapsedTime)}"
    } else {
        Strings.APP_TITLE
    }
}

fun createTrayIcon(currentTask: String?, elapsedTime: Duration, showColon: Boolean): BufferedImage {
    // macOS Menu Bar Icons: Template Images mit @2x für Retina
    // Standard-Höhe: 22pt (44px @2x)
    // Breite variabel, aber kompakt wie die Uhrzeit
    val height = 44  // @2x für Retina
    val width = 60   // Kompakt für "H:MM" Format (wie macOS Uhrzeit)

    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g = img.createGraphics()

    // High-Quality Rendering für Retina
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)

    // Transparenter Hintergrund
    g.composite = AlphaComposite.Clear
    g.fillRect(0, 0, width, height)
    g.composite = AlphaComposite.SrcOver

    // Schwarzer Text (Template Image - macOS invertiert automatisch)
    g.color = Color.WHITE

    // System-Font ähnlich wie macOS Menüleiste (SF Pro auf macOS)
    // Wir verwenden SansSerif mit normaler Stärke für bessere Lesbarkeit
    val fontSize = 26  // @2x Größe, entspricht ~13pt
    g.font = Font(".AppleSystemUIFont", Font.PLAIN, fontSize)

    // Fallback falls System-Font nicht verfügbar
    if (g.font.family == "Dialog") {
        g.font = Font(Font.SANS_SERIF, Font.PLAIN, fontSize)
    }

    val text = formatDurationForTray(duration = elapsedTime, showColon = showColon)
    val fontMetrics = g.fontMetrics

    // Zentriere den Text
    val textWidth = fontMetrics.stringWidth(text)
    val x = (width - textWidth) / 2
    val y = (height - fontMetrics.height) / 2 + fontMetrics.ascent

    g.drawString(text, x, y)

    g.dispose()
    return img
}
