package `fun`.abbas.android_res_translator.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import `fun`.abbas.android_res_translator.ui.settings.AppAppearance

/**
 * [DESIGN_SKIN_BLACK.md] Geek Abyss — 仅 YAML `colors` 块映射到 Material3。
 */
internal val GeekAbyssColorScheme: ColorScheme =
    darkColorScheme(
        primary = colorFromHex("#e1fdff"),
        onPrimary = colorFromHex("#00363a"),
        primaryContainer = colorFromHex("#00f2ff"),
        onPrimaryContainer = colorFromHex("#006a71"),
        inversePrimary = colorFromHex("#00696f"),
        secondary = colorFromHex("#d7ffc5"),
        onSecondary = colorFromHex("#053900"),
        secondaryContainer = colorFromHex("#2ff801"),
        onSecondaryContainer = colorFromHex("#0f6d00"),
        tertiary = colorFromHex("#f7f8f8"),
        onTertiary = colorFromHex("#2f3131"),
        tertiaryContainer = colorFromHex("#dbdbdb"),
        onTertiaryContainer = colorFromHex("#5e6060"),
        background = colorFromHex("#131313"),
        onBackground = colorFromHex("#e5e2e1"),
        surface = colorFromHex("#131313"),
        onSurface = colorFromHex("#e5e2e1"),
        surfaceVariant = colorFromHex("#353534"),
        onSurfaceVariant = colorFromHex("#b9cacb"),
        surfaceTint = colorFromHex("#00dbe7"),
        inverseSurface = colorFromHex("#e5e2e1"),
        inverseOnSurface = colorFromHex("#313030"),
        outline = colorFromHex("#849495"),
        outlineVariant = colorFromHex("#3a494b"),
        error = colorFromHex("#ffb4ab"),
        onError = colorFromHex("#690005"),
        errorContainer = colorFromHex("#93000a"),
        onErrorContainer = colorFromHex("#ffdad6"),
        surfaceContainerLowest = colorFromHex("#0e0e0e"),
        surfaceContainerLow = colorFromHex("#1c1b1b"),
        surfaceContainer = colorFromHex("#201f1f"),
        surfaceContainerHigh = colorFromHex("#2a2a2a"),
        surfaceContainerHighest = colorFromHex("#353534"),
    )

/**
 * [DESIGN_SKIN_WHITE.md] Minimalist Porcelain — 仅 YAML `colors` 块映射到 Material3（浅色）。
 */
internal val MinimalistPorcelainColorScheme: ColorScheme =
    lightColorScheme(
        primary = colorFromHex("#003ec7"),
        onPrimary = colorFromHex("#ffffff"),
        primaryContainer = colorFromHex("#0052ff"),
        onPrimaryContainer = colorFromHex("#dfe3ff"),
        inversePrimary = colorFromHex("#b7c4ff"),
        secondary = colorFromHex("#5b5f61"),
        onSecondary = colorFromHex("#ffffff"),
        secondaryContainer = colorFromHex("#e0e3e6"),
        onSecondaryContainer = colorFromHex("#626567"),
        tertiary = colorFromHex("#4c4e4f"),
        onTertiary = colorFromHex("#ffffff"),
        tertiaryContainer = colorFromHex("#656666"),
        onTertiaryContainer = colorFromHex("#e4e5e5"),
        background = colorFromHex("#f9f9fc"),
        onBackground = colorFromHex("#1a1c1e"),
        surface = colorFromHex("#f9f9fc"),
        onSurface = colorFromHex("#1a1c1e"),
        surfaceVariant = colorFromHex("#e2e2e5"),
        onSurfaceVariant = colorFromHex("#434656"),
        surfaceTint = colorFromHex("#004ced"),
        inverseSurface = colorFromHex("#2f3133"),
        inverseOnSurface = colorFromHex("#f0f0f3"),
        outline = colorFromHex("#737688"),
        outlineVariant = colorFromHex("#c3c5d9"),
        error = colorFromHex("#ba1a1a"),
        onError = colorFromHex("#ffffff"),
        errorContainer = colorFromHex("#ffdad6"),
        onErrorContainer = colorFromHex("#93000a"),
        surfaceContainerLowest = colorFromHex("#ffffff"),
        surfaceContainerLow = colorFromHex("#f3f3f6"),
        surfaceContainer = colorFromHex("#eeeef0"),
        surfaceContainerHigh = colorFromHex("#e8e8ea"),
        surfaceContainerHighest = colorFromHex("#e2e2e5"),
    )

fun AppAppearance.resolveColorScheme(): ColorScheme =
    when (this) {
        AppAppearance.Classic -> AppDarkColorScheme
        AppAppearance.GeekAbyss -> GeekAbyssColorScheme
        AppAppearance.MinimalistPorcelain -> MinimalistPorcelainColorScheme
    }
