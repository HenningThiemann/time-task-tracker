package util

import java.io.File
import javax.imageio.ImageIO

/**
 * Helper to generate the app icon file for distribution.
 * Run this to create the icon file for macOS distribution.
 */
fun main() {
    val outputDir = File("src/main/resources")
    outputDir.mkdirs()

    // Generate different sizes for the icon
    val sizes = listOf(16, 32, 64, 128, 256, 512, 1024)

    println("Generating stopwatch icons...")

    for (size in sizes) {
        val icon = AppIconUtils.createStopwatchIcon(size)
        val outputFile = File(outputDir, "icon_${size}x${size}.png")
        ImageIO.write(icon, "png", outputFile)
        println("Created: ${outputFile.absolutePath}")
    }

    println("\nTo create the macOS ICNS file, run:")
    println("cd src/main/resources")
    println("mkdir app-icon.iconset")
    println("sips -z 16 16     icon_16x16.png     --out app-icon.iconset/icon_16x16.png")
    println("sips -z 32 32     icon_32x32.png     --out app-icon.iconset/icon_16x16@2x.png")
    println("sips -z 32 32     icon_32x32.png     --out app-icon.iconset/icon_32x32.png")
    println("sips -z 64 64     icon_64x64.png     --out app-icon.iconset/icon_32x32@2x.png")
    println("sips -z 128 128   icon_128x128.png   --out app-icon.iconset/icon_128x128.png")
    println("sips -z 256 256   icon_256x256.png   --out app-icon.iconset/icon_128x128@2x.png")
    println("sips -z 256 256   icon_256x256.png   --out app-icon.iconset/icon_256x256.png")
    println("sips -z 512 512   icon_512x512.png   --out app-icon.iconset/icon_256x256@2x.png")
    println("sips -z 512 512   icon_512x512.png   --out app-icon.iconset/icon_512x512.png")
    println("sips -z 1024 1024 icon_1024x1024.png --out app-icon.iconset/icon_512x512@2x.png")
    println("iconutil -c icns app-icon.iconset")
    println("rm -rf app-icon.iconset")

    println("\nIcons generated successfully!")
}

