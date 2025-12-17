package ui.theme

import androidx.compose.runtime.mutableStateOf

object ThemeManager {
    val isDarkMode = mutableStateOf(true)

    fun toggleTheme() {
        isDarkMode.value = !isDarkMode.value
    }
}

