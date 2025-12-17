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
import config.Strings
import manager.TaskManager
import model.CompletedTask
import model.Project
import ui.components.ProjectItem

@Composable
fun ProjectsTab(
    taskManager: TaskManager,
    projects: List<Project>,
    allTasks: List<CompletedTask>,
    onCreateProject: (String) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onTaskResumed: () -> Unit = {}
) {
    var newProjectName by remember { mutableStateOf("") }

    // Gruppiere Tasks nach Projekt
    val tasksByProject = remember(allTasks, projects) {
        projects.associateWith { project ->
            allTasks.filter { it.project == project.name }
        }
    }

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
                LazyColumn(
                    modifier = Modifier.padding(16.dp).fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(projects) { project ->
                        ProjectItem(
                            project = project,
                            tasks = tasksByProject[project] ?: emptyList(),
                            onDelete = { onDeleteProject(project.id) },
                            onResumeTask = { task ->
                                taskManager.restartTask(task)
                                onTaskResumed()
                            },
                            onDeleteTask = { task ->
                                taskManager.deleteTask(task)
                            }
                        )
                    }
                }
            }
        }
    }
}

