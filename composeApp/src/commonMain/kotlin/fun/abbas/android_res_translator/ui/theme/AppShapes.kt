package `fun`.abbas.android_res_translator.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * DESIGN.md Shapes：
 * - 卡片/容器 12px (lg)
 * - 按钮/输入 8px (base)
 * - 选中指示 2px
 */
val AppShapes =
    Shapes(
        extraSmall = RoundedCornerShape(2.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(8.dp),
        large = RoundedCornerShape(12.dp),
        extraLarge = RoundedCornerShape(16.dp),
    )

val AppCardShape = RoundedCornerShape(12.dp)
val AppControlShape = RoundedCornerShape(8.dp)
/** 大屏永久侧栏容器（矩形圆角，非胶囊形） */
val AppSidebarShape = RoundedCornerShape(12.dp)
val AppIndicatorShape = RoundedCornerShape(2.dp)
