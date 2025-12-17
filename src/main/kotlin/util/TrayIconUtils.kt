package util

import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.time.Duration

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

