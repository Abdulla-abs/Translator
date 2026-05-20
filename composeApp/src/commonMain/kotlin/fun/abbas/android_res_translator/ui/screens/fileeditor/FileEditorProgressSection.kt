package `fun`.abbas.android_res_translator.ui.screens.fileeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import `fun`.abbas.android_res_translator.ui.components.AppGlassCard
import `fun`.abbas.android_res_translator.ui.components.AppThinProgress
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing

@Composable
fun FileEditorProgressCard(
    state: FileEditorState,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val percentInt = (state.progressPercent * 100).toInt()
    AppGlassCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text("Overall Progress", style = MaterialTheme.typography.headlineSmall)
            Text(
                "$percentInt%",
                style = MaterialTheme.typography.displaySmall,
                color = colors.primary,
            )
        }
        AppThinProgress(
            progress = state.progressPercent.coerceIn(0f, 1f),
            inProgress = state.isRunning,
            modifier = Modifier.padding(vertical = AppSpacing.md),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            StatColumn("Total", state.totalCount.toString())
            StatColumn("Translated", state.completedCount.toString(), colors.primary)
            StatColumn("Pending", state.pendingCount.toString(), colors.tertiary)
        }
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Column {
        Text(label, style = AppLabelCapsTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.headlineSmall, color = valueColor)
    }
}
