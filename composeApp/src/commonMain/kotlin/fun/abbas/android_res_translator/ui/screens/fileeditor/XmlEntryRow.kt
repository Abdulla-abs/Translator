package `fun`.abbas.android_res_translator.ui.screens.fileeditor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.theme.AppCodeSmallTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppControlShape
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.common_delete
import androidrestranslator.composeapp.generated.resources.common_edit
import androidrestranslator.composeapp.generated.resources.common_retry
import androidrestranslator.composeapp.generated.resources.file_editor_translation_failed
import org.jetbrains.compose.resources.stringResource
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing

/** 首版隐藏条目行操作按钮；保留实现供后续启用。 */
private const val ShowEntryActionButtons = false

@Composable
fun XmlEntryRow(
    entry: XmlEntryUi,
    sourceLang: String,
    targetLang: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = AppControlShape,
                    color = colors.secondaryContainer.copy(alpha = 0.2f),
                ) {
                    Text(
                        entry.key,
                        style = AppCodeSmallTextStyle,
                        color = colors.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                    )
                }
                EntryStatusChip(
                    status = entry.status,
                    modifier = Modifier.padding(start = AppSpacing.sm),
                )
            }
            BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md)) {
                if (maxWidth >= 600.dp) {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
                        SourceTargetCell(
                            label = "Source ($sourceLang)",
                            text = entry.sourceText,
                            isTarget = false,
                            modifier = Modifier.weight(1f),
                        )
                        TargetCell(entry, targetLang, Modifier.weight(1f))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                        SourceTargetCell("Source ($sourceLang)", entry.sourceText, false, Modifier.fillMaxWidth())
                        TargetCell(entry, targetLang, Modifier.fillMaxWidth())
                    }
                }
            }
        }
        if (ShowEntryActionButtons) {
            Column {
                when (entry.status) {
                    is EntryStatus.Error -> {
                        IconButton(onClick = onRetry) {
                            Icon(Icons.Default.Refresh, contentDescription = stringResource(Res.string.common_retry), tint = colors.primary)
                        }
                        IconButton(onClick = { /* 首版不删除 */ }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(Res.string.common_delete), tint = colors.onSurfaceVariant)
                        }
                    }
                    is EntryStatus.Completed -> {
                        IconButton(onClick = { /* 首版不编辑 */ }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.common_edit), tint = colors.onSurfaceVariant)
                        }
                    }
                    else -> {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = colors.onSurfaceVariant.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryStatusChip(
    status: EntryStatus,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val (label, icon, fg, bg) =
        when (status) {
            EntryStatus.Completed ->
                Quad("Completed", Icons.Default.CheckCircle, colors.primary, colors.primary.copy(alpha = 0.1f))
            EntryStatus.Translating ->
                Quad("Translating...", Icons.Default.Schedule, colors.tertiary, colors.tertiary.copy(alpha = 0.1f))
            EntryStatus.Pending ->
                Quad("Pending", Icons.Default.Schedule, colors.onSurfaceVariant, colors.surfaceContainerHigh)
            is EntryStatus.Error ->
                Quad("Error", Icons.Default.Error, colors.error, colors.error.copy(alpha = 0.1f))
        }
    Surface(
        modifier = modifier,
        shape = AppControlShape,
        color = bg,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.padding(end = 4.dp))
            Text(
                label,
                style = AppLabelCapsTextStyle,
                color = fg,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

@Composable
private fun SourceTargetCell(
    label: String,
    text: String,
    isTarget: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = AppControlShape,
        color = if (isTarget) colors.surfaceContainerHigh else colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.3f)),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md)) {
            Text(label.uppercase(), style = AppLabelCapsTextStyle, color = colors.onSurfaceVariant)
            Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = AppSpacing.xs))
        }
    }
}

@Composable
private fun TargetCell(
    entry: XmlEntryUi,
    targetLang: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    when (entry.status) {
        EntryStatus.Translating -> {
            Surface(
                modifier = modifier,
                shape = AppControlShape,
                color = colors.surfaceContainerHigh,
                border = BorderStroke(1.dp, colors.outlineVariant),
            ) {
                Row(
                    modifier = Modifier.padding(AppSpacing.md).fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, tint = colors.onSurfaceVariant)
                    Text(
                        "AI is thinking...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(start = AppSpacing.sm),
                    )
                }
            }
        }
        is EntryStatus.Error -> {
            val message = entry.status.message
            Surface(
                modifier = modifier,
                shape = AppControlShape,
                color = colors.surfaceContainerHigh,
                border = BorderStroke(1.dp, colors.error.copy(alpha = 0.5f)),
            ) {
                Column(modifier = Modifier.padding(AppSpacing.md)) {
                    Text(
                        stringResource(Res.string.file_editor_translation_failed),
                        style = AppLabelCapsTextStyle,
                        color = colors.error,
                    )
                    Text(
                        message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = AppSpacing.xs),
                    )
                }
            }
        }
        else -> {
            SourceTargetCell(
                label = "Target ($targetLang)",
                text = entry.targetText ?: "—",
                isTarget = true,
                modifier = modifier,
            )
        }
    }
}
