package `fun`.abbas.android_res_translator.ui.screens.fileeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.components.AppGlassCard
import `fun`.abbas.android_res_translator.ui.components.AppLanguageChip
import `fun`.abbas.android_res_translator.ui.components.AppThinProgress
import `fun`.abbas.android_res_translator.ui.screens.main.formatLanguageLabel
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.file_editor_overall_progress
import androidrestranslator.composeapp.generated.resources.file_editor_stat_error
import androidrestranslator.composeapp.generated.resources.file_editor_stat_pending
import androidrestranslator.composeapp.generated.resources.file_editor_stat_total
import androidrestranslator.composeapp.generated.resources.file_editor_stat_translated
import androidrestranslator.composeapp.generated.resources.file_editor_swap_langs
import org.jetbrains.compose.resources.stringResource

@Composable
fun FileEditorProgressCard(
    state: FileEditorState,
    onEditSourceLang: () -> Unit,
    onEditTargetLang: () -> Unit,
    onSwapLanguages: () -> Unit,
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
            Text(stringResource(Res.string.file_editor_overall_progress), style = MaterialTheme.typography.headlineSmall)
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
            StatColumn(stringResource(Res.string.file_editor_stat_total), state.totalCount.toString())
            StatColumn(stringResource(Res.string.file_editor_stat_translated), state.completedCount.toString(), colors.primary)
            StatColumn(stringResource(Res.string.file_editor_stat_pending), state.pendingCount.toString(), colors.tertiary)
            if (state.errorCount > 0) {
                StatColumn(stringResource(Res.string.file_editor_stat_error), state.errorCount.toString(), colors.error)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppLanguageChip(
                label = formatLanguageLabel(state.sourceLang),
                onClick = onEditSourceLang,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            Surface(
                onClick = onSwapLanguages,
                shape = CircleShape,
                color = colors.primaryContainer.copy(alpha = 0.35f),
                modifier = Modifier.size(40.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = stringResource(Res.string.file_editor_swap_langs),
                        tint = colors.primary,
                    )
                }
            }
            AppLanguageChip(
                label = formatLanguageLabel(state.targetLang),
                onClick = onEditTargetLang,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
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
