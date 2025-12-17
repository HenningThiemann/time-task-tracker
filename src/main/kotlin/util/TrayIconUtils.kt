package util

import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.time.Duration

fun createTrayIcon(currentTask: String?, elapsedTime: Duration, showColon: Boolean): BufferedImage {
    // macOS Menu Bar Icons: Template Images with @2x for Retina
    // Standard height: 22pt (44px @2x)
    // Width variable, but compact like the clock time
    val height = 44  // @2x for Retina
    val width = 60   // Compact for "H:MM" format (like macOS clock)

    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g = img.createGraphics()

    // High-Quality Rendering for Retina
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)

    // Transparent background
    g.composite = AlphaComposite.Clear
    g.fillRect(0, 0, width, height)
    g.composite = AlphaComposite.SrcOver

    // Black text (Template Image - macOS inverts automatically)
    g.color = Color.WHITE

    // System font similar to macOS menu bar (SF Pro on macOS)
    // We use SansSerif with normal weight for better readability
    val fontSize = 26  // @2x size, corresponds to ~13pt
    g.font = Font(".AppleSystemUIFont", Font.PLAIN, fontSize)

    // Fallback if system font is not available
    if (g.font.family == "Dialog") {
        g.font = Font(Font.SANS_SERIF, Font.PLAIN, fontSize)
    }

    val text = formatDurationForTray(duration = elapsedTime, showColon = showColon)
    val fontMetrics = g.fontMetrics

    // Center the text
    val textWidth = fontMetrics.stringWidth(text)
    val x = (width - textWidth) / 2
    val y = (height - fontMetrics.height) / 2 + fontMetrics.ascent

    g.drawString(text, x, y)

    g.dispose()
    return img
}

