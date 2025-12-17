package util

import java.awt.*
import java.awt.image.BufferedImage

/**
 * Utility to create a stopwatch icon for the application dock/window.
 */
object AppIconUtils {

    fun createStopwatchIcon(size: Int = 512): BufferedImage {
        val img = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()

        // Enable anti-aliasing for smooth graphics
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        // Background - transparent
        g.composite = AlphaComposite.Clear
        g.fillRect(0, 0, size, size)
        g.composite = AlphaComposite.SrcOver

        // Define colors
        val primaryColor = Color(33, 150, 243) // Blue
        val accentColor = Color(255, 152, 0) // Orange
        val darkGray = Color(60, 60, 60)

        // Calculate dimensions
        val centerX = size / 2
        val centerY = size / 2 + size / 10 // Slightly lower for the button
        val mainRadius = (size * 0.38).toInt()
        val buttonRadius = (size * 0.08).toInt()
        val buttonY = centerY - mainRadius - buttonRadius - (size * 0.05).toInt()

        // Draw button/crown at the top
        g.color = darkGray
        g.fillOval(centerX - buttonRadius, buttonY - buttonRadius, buttonRadius * 2, buttonRadius * 2)

        // Draw button stem
        val stemWidth = buttonRadius / 2
        val stemHeight = (size * 0.04).toInt()
        g.fillRect(centerX - stemWidth / 2, buttonY + buttonRadius, stemWidth, stemHeight)

        // Draw outer shadow
        g.color = Color(0, 0, 0, 30)
        g.fillOval(
            centerX - mainRadius - 3,
            centerY - mainRadius - 3,
            mainRadius * 2 + 6,
            mainRadius * 2 + 6
        )

        // Draw main circle with gradient
        val gradient = GradientPaint(
            (centerX - mainRadius).toFloat(),
            (centerY - mainRadius).toFloat(),
            Color(250, 250, 250),
            (centerX + mainRadius).toFloat(),
            (centerY + mainRadius).toFloat(),
            Color(220, 220, 220)
        )
        g.paint = gradient
        g.fillOval(centerX - mainRadius, centerY - mainRadius, mainRadius * 2, mainRadius * 2)

        // Draw outer rim
        g.color = darkGray
        val rimThickness = (size * 0.02).toInt()
        g.stroke = BasicStroke(rimThickness.toFloat())
        g.drawOval(centerX - mainRadius, centerY - mainRadius, mainRadius * 2, mainRadius * 2)

        // Draw hour markers
        g.color = darkGray
        val markerLength = (size * 0.04).toInt()
        val markerInnerRadius = mainRadius - (size * 0.06).toInt()

        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30 - 90).toDouble())
            val x1 = centerX + (markerInnerRadius * Math.cos(angle)).toInt()
            val y1 = centerY + (markerInnerRadius * Math.sin(angle)).toInt()
            val x2 = centerX + ((markerInnerRadius + markerLength) * Math.cos(angle)).toInt()
            val y2 = centerY + ((markerInnerRadius + markerLength) * Math.sin(angle)).toInt()

            g.stroke = if (i % 3 == 0) {
                BasicStroke((size * 0.008).toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            } else {
                BasicStroke((size * 0.004).toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            }
            g.drawLine(x1, y1, x2, y2)
        }

        // Draw clock hands (showing 10:10 - a classic watch pose)

        // Hour hand (pointing to 10)
        val hourAngle = Math.toRadians((10 * 30 - 90).toDouble())
        val hourLength = (mainRadius * 0.5).toInt()
        g.color = primaryColor
        g.stroke = BasicStroke((size * 0.015).toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        g.drawLine(
            centerX,
            centerY,
            centerX + (hourLength * Math.cos(hourAngle)).toInt(),
            centerY + (hourLength * Math.sin(hourAngle)).toInt()
        )

        // Minute hand (pointing to 2)
        val minuteAngle = Math.toRadians((10 * 6 - 90).toDouble())
        val minuteLength = (mainRadius * 0.7).toInt()
        g.color = primaryColor
        g.stroke = BasicStroke((size * 0.012).toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        g.drawLine(
            centerX,
            centerY,
            centerX + (minuteLength * Math.cos(minuteAngle)).toInt(),
            centerY + (minuteLength * Math.sin(minuteAngle)).toInt()
        )

        // Second hand (pointing to 12 - like a stopwatch ready to start)
        val secondAngle = Math.toRadians(-90.0)
        val secondLength = (mainRadius * 0.75).toInt()
        g.color = accentColor
        g.stroke = BasicStroke((size * 0.006).toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        g.drawLine(
            centerX,
            centerY,
            centerX + (secondLength * Math.cos(secondAngle)).toInt(),
            centerY + (secondLength * Math.sin(secondAngle)).toInt()
        )

        // Draw center dot
        val dotRadius = (size * 0.03).toInt()
        g.color = accentColor
        g.fillOval(centerX - dotRadius, centerY - dotRadius, dotRadius * 2, dotRadius * 2)

        // Draw inner highlight on center dot
        g.color = Color(255, 200, 100)
        val highlightRadius = dotRadius / 2
        g.fillOval(
            centerX - highlightRadius,
            centerY - highlightRadius - highlightRadius / 2,
            highlightRadius * 2,
            highlightRadius * 2
        )

        g.dispose()
        return img
    }
}

