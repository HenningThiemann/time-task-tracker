package model

import java.time.Duration
import java.time.LocalDateTime

data class CompletedTask(
    val name: String,
    val project: String? = null,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val duration: Duration
)

data class RunningTask(
    val id: Long,
    val name: String,
    val project: String? = null,
    val startTime: LocalDateTime,
    val accumulatedDuration: Duration
)

