import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import kotlinx.coroutines.delay
import java.awt.SystemTray
import java.awt.Tray
import java.awt.TrayIcon
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main() = application {
    var isVisible by remember { mutableStateOf(false) }
    val taskManager = remember { TaskManager() }

    // Update menu bar every second
    LaunchedEffect(Unit) {
        while (true) {
            taskManager.updateElapsedTime()
            delay(1000)
        }
    }

    // Menu bar tray icon
    Tray(
        icon = createTrayIcon(taskManager.currentTask.value, taskManager.elapsedTime.value),
        tooltip = "Time Task Tracker",
        menu = {
            Item("Show Window") {
                isVisible = true
            }
            Separator()
            Item("Quit") {
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
            title = "Time Task Tracker",
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
                text = "Time Task Tracker",
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
                        text = if (currentTask != null) "Aktuelle Aufgabe" else "Keine aktive Aufgabe",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (currentTask != null) {
                        Text(
                            text = currentTask!!,
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
                    } else {
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
                label = { Text("Aufgabenname") },
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
                    Text("Start")
                }

                Button(
                    onClick = { taskManager.stopTask() },
                    modifier = Modifier.weight(1f),
                    enabled = currentTask != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Stop")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Task History
            if (taskHistory.isNotEmpty()) {
                Text(
                    text = "Verlauf",
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
                        taskHistory.reversed().take(10).forEach { task ->
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

    fun startTask(taskName: String) {
        currentTask.value = taskName
        startTime = LocalDateTime.now()
        elapsedTime.value = Duration.ZERO
    }

    fun stopTask() {
        currentTask.value?.let { name ->
            val endTime = LocalDateTime.now()
            val duration = Duration.between(startTime, endTime)
            
            val completedTask = CompletedTask(
                name = name,
                startTime = startTime!!,
                endTime = endTime,
                duration = duration
            )
            
            taskHistory.value = taskHistory.value + completedTask
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

fun createTrayIcon(currentTask: String?, elapsedTime: Duration): BufferedImage {
    val size = 22
    val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
    val g = image.createGraphics()
    
    try {
        // Clear background
        g.color = java.awt.Color(0, 0, 0, 0)
        g.fillRect(0, 0, size, size)
        
        // Draw icon
        if (currentTask != null) {
            // Active - draw filled circle with time
            g.color = java.awt.Color(76, 175, 80) // Green
            g.fillOval(2, 2, size - 4, size - 4)
            
            // Draw time text
            g.color = java.awt.Color.WHITE
            g.font = java.awt.Font("Arial", java.awt.Font.BOLD, 10)
            val timeText = "${elapsedTime.toMinutes()}m"
            val metrics = g.fontMetrics
            val x = (size - metrics.stringWidth(timeText)) / 2
            val y = (size - metrics.height) / 2 + metrics.ascent
            g.drawString(timeText, x, y)
        } else {
            // Inactive - draw hollow circle
            g.color = java.awt.Color(158, 158, 158) // Gray
            g.fillOval(2, 2, size - 4, size - 4)
            g.color = java.awt.Color(224, 224, 224)
            g.fillOval(4, 4, size - 8, size - 8)
        }
    } finally {
        g.dispose()
    }
    
    return image
}
