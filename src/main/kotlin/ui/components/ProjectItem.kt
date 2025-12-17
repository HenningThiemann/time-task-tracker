package ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.CompletedTask
import model.Project
import util.formatDuration
import java.time.format.DateTimeFormatter

@Composable
fun ProjectItem(
    project: Project,
    tasks: List<CompletedTask>,
    onDelete: () -> Unit,
    onResumeTask: (CompletedTask) -> Unit,
    onDeleteTask: (CompletedTask) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Project Header - clickable to expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "▼" else "▶",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Column {
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "${tasks.size} Aufgabe(n)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { onDelete() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Text(
                        text = "🗑",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Expandable Task List
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

                    if (tasks.isEmpty()) {
                        Text(
                            text = "Keine Aufgaben für dieses Projekt",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        tasks.forEach { task ->
                            ProjectTaskItem(
                                task = task,
                                onResume = { onResumeTask(task) },
                                onDelete = { onDeleteTask(task) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectTaskItem(
    task: CompletedTask,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

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
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onResume,
                modifier = Modifier.size(32.dp)
            ) {
                Text(
                    text = "▶",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Text(
                    text = "🗑",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

