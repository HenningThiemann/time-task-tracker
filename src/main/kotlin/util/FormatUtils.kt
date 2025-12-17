package util

import java.time.Duration

fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutesPart()
    val seconds = duration.toSecondsPart()
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

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
        config.Strings.APP_TITLE
    }
}

