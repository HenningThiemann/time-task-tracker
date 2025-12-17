package ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import config.Config
import config.Strings
import manager.TaskManager
import model.CompletedTask
import model.Project
import ui.components.CurrentTaskCard
import ui.components.TaskHistoryItem
import java.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksTab(
    taskManager: TaskManager,
    currentTask: String?,
    currentProject: String?,
    elapsedTime: Duration,
    taskHistory: List<CompletedTask>,
    projects: List<Project>,
    isPaused: Boolean,
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
        CurrentTaskCard(
            currentTask = currentTask,
            currentProject = currentProject,
            elapsedTime = elapsedTime
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Task Input and Controls - Aufgabenname und Projekt in einer Row
        var expandedProjectDropdown by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Aufgabenname
            OutlinedTextField(
                value = taskName,
                onValueChange = onTaskNameChange,
                label = { Text(Strings.TASK_NAME_LABEL) },
                modifier = Modifier.weight(1f),
                enabled = currentTask == null,
                singleLine = true
            )

            // Projekt-Auswahl
            ExposedDropdownMenuBox(
                expanded = expandedProjectDropdown,
                onExpandedChange = { expandedProjectDropdown = !expandedProjectDropdown && currentTask == null },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = onProjectNameChange,
                    label = { Text(Strings.PROJECT_LABEL) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Start-Button: nur aktiv wenn kein Task läuft
            Button(
                onClick = onStartTask,
                modifier = Modifier.weight(1f),
                enabled = currentTask == null && taskName.isNotBlank()
            ) {
                Text(Strings.BUTTON_START)
            }

            // Fortsetzen-Button: nur sichtbar wenn ein Task pausiert ist
            if (currentTask != null && isPaused) {
                Button(
                    onClick = { taskManager.resumeTask() },
                    modifier = Modifier.weight(1f),
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(Strings.BUTTON_RESUME)
                }
            }

            // Stop-Button: nur aktiv wenn ein Task läuft (auch pausiert)
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
                LazyColumn(
                    modifier = Modifier.padding(16.dp).fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(taskHistory.reversed().take(Config.MAX_HISTORY_ITEMS_DISPLAYED)) { task ->
                        TaskHistoryItem(
                            task = task,
                            onRestart = { completedTask ->
                                if (currentTask == null) {
                                    taskManager.restartTask(completedTask)
                                }
                            },
                            onDelete = { completedTask ->
                                taskManager.deleteTask(completedTask)
                            }
                        )
                    }
                }
            }
        }
    }
}

