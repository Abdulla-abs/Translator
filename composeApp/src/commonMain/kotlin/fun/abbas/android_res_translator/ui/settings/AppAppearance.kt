package `fun`.abbas.android_res_translator.ui.settings

/**
 * 应用配色预设（仅颜色；排版、圆角与间距仍使用工程内统一的 Typography / Shapes / Spacing）。
 */
enum class AppAppearance {
    /** 原 DESIGN.md Kotlin Purple 深色主题 */
    Classic,

    /** [DESIGN_SKIN_BLACK.md] Geek Abyss */
    GeekAbyss,

    /** [DESIGN_SKIN_WHITE.md] Minimalist Porcelain（浅色） */
    MinimalistPorcelain,
    ;

    companion object {
        fun fromPersisted(value: String?): AppAppearance =
            entries.firstOrNull { it.name == value } ?: Classic
    }
}
