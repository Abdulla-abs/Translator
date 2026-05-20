package `fun`.abbas.android_res_translator.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/** 将 [DESIGN.md](../../../DESIGN.md) YAML 中的十六进制色值转为 [Color]。 */
internal fun colorFromHex(hex: String): Color {
    val normalized = hex.removePrefix("#")
    require(normalized.length == 6) { "Expected #RRGGBB, got $hex" }
    return Color(
        red = normalized.substring(0, 2).toInt(16) / 255f,
        green = normalized.substring(2, 4).toInt(16) / 255f,
        blue = normalized.substring(4, 6).toInt(16) / 255f,
    )
}

/**
 * Material3 深色配色，与 DESIGN.md `colors` 块一一对应。
 * 主色 Kotlin Purple、次色 Developer Blue，默认深色 IDE 风格。
 */
val AppDarkColorScheme =
    darkColorScheme(
        primary = colorFromHex("#cdbdff"),
        onPrimary = colorFromHex("#370095"),
        primaryContainer = colorFromHex("#7f52ff"),
        onPrimaryContainer = colorFromHex("#fffdff"),
        inversePrimary = colorFromHex("#6835e7"),
        secondary = colorFromHex("#b4c5ff"),
        onSecondary = colorFromHex("#002a78"),
        secondaryContainer = colorFromHex("#0053db"),
        onSecondaryContainer = colorFromHex("#cdd7ff"),
        tertiary = colorFromHex("#ffb783"),
        onTertiary = colorFromHex("#4f2500"),
        tertiaryContainer = colorFromHex("#b55d00"),
        onTertiaryContainer = colorFromHex("#fffcff"),
        background = colorFromHex("#0b1326"),
        onBackground = colorFromHex("#dae2fd"),
        surface = colorFromHex("#0b1326"),
        onSurface = colorFromHex("#dae2fd"),
        surfaceVariant = colorFromHex("#2d3449"),
        onSurfaceVariant = colorFromHex("#cac3d8"),
        surfaceTint = colorFromHex("#cdbdff"),
        inverseSurface = colorFromHex("#dae2fd"),
        inverseOnSurface = colorFromHex("#283044"),
        outline = colorFromHex("#948ea1"),
        outlineVariant = colorFromHex("#494455"),
        error = colorFromHex("#ffb4ab"),
        onError = colorFromHex("#690005"),
        errorContainer = colorFromHex("#93000a"),
        onErrorContainer = colorFromHex("#ffdad6"),
        surfaceContainerLowest = colorFromHex("#060e20"),
        surfaceContainerLow = colorFromHex("#131b2e"),
        surfaceContainer = colorFromHex("#171f33"),
        surfaceContainerHigh = colorFromHex("#222a3d"),
        surfaceContainerHighest = colorFromHex("#2d3449"),
    )

/** 描边/分隔线标准线宽（DESIGN.md Shapes：1.5px）。 */
val AppOutlineStrokeWidth = 1.5f
