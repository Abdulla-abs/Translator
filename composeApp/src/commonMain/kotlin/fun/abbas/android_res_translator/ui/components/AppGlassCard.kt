package `fun`.abbas.android_res_translator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.theme.AppCardShape
import `fun`.abbas.android_res_translator.ui.theme.AppOutlineStrokeWidth
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing

@Composable
fun AppGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppCardShape,
        color = colors.surfaceContainer.copy(alpha = 0.85f),
        border = BorderStroke(AppOutlineStrokeWidth.dp, colors.outlineVariant.copy(alpha = 0.5f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(Modifier.padding(AppSpacing.lg), content = content)
    }
}
