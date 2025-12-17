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
    const val BUTTON_START = "Start"
    const val BUTTON_STOP = "Stop"
    const val HISTORY_TITLE = "Verlauf"
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
    val elapsedTime by taskManager.elapsedTime
    val taskHistory by taskManager.taskHistory

    var taskName by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = Strings.APP_TITLE,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                onValueChange = { taskName = it },
                label = { Text(Strings.TASK_NAME_LABEL) },
                modifier = Modifier.fillMaxWidth(),
                enabled = currentTask == null,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (taskName.isNotBlank()) {
                            taskManager.startTask(taskName)
                            taskName = ""
                        }
                    },
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
                            TaskHistoryItem(task)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskHistoryItem(task: CompletedTask) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = task.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
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
    }
}

class TaskManager {
    val currentTask = mutableStateOf<String?>(null)
    val elapsedTime = mutableStateOf(Duration.ZERO)
    val taskHistory = mutableStateOf<List<CompletedTask>>(emptyList())

    private var startTime: LocalDateTime? = null

    companion object {
        private const val MAX_HISTORY_SIZE = 100 // Keep last 100 tasks to prevent unbounded growth
    }

    fun startTask(taskName: String) {
        currentTask.value = taskName
        startTime = LocalDateTime.now()
        elapsedTime.value = Duration.ZERO
    }

    fun stopTask() {
        val task = currentTask.value
        val start = startTime

        if (task != null && start != null) {
            val endTime = LocalDateTime.now()
            val duration = Duration.between(start, endTime)

            val completedTask = CompletedTask(
                name = task,
                startTime = start,
                endTime = endTime,
                duration = duration
            )

            // Add new task and keep only the last MAX_HISTORY_SIZE entries
            val newHistory = (taskHistory.value + completedTask).takeLast(MAX_HISTORY_SIZE)
            taskHistory.value = newHistory
        }

        currentTask.value = null
        startTime = null
        elapsedTime.value = Duration.ZERO
    }

    fun updateElapsedTime() {
        startTime?.let {
            elapsedTime.value = Duration.between(it, LocalDateTime.now())
        }
    }
}

data class CompletedTask(
    val name: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val duration: Duration
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
