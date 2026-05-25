package `fun`.abbas.android_res_translator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.screens.compare.CompareProject
import `fun`.abbas.android_res_translator.util.currentEpochMillis
import `fun`.abbas.android_res_translator.ui.theme.AppCodeSmallTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.compare_project_files_ready
import androidrestranslator.composeapp.generated.resources.compare_project_status_partial
import androidrestranslator.composeapp.generated.resources.compare_project_status_pending
import androidrestranslator.composeapp.generated.resources.compare_project_status_ready
import androidrestranslator.composeapp.generated.resources.compare_project_upload_progress
import androidrestranslator.composeapp.generated.resources.file_project_modified_days
import androidrestranslator.composeapp.generated.resources.file_project_modified_hours
import androidrestranslator.composeapp.generated.resources.file_project_modified_just_now
import org.jetbrains.compose.resources.stringResource

@Composable
fun CompareProjectCard(
    project: CompareProject,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val ready = project.isReadyToCompare
    val accent = if (ready) colors.tertiary else colors.primary
    val accentContainer = if (ready) colors.tertiaryContainer else colors.primaryContainer
    val uploadedCount = (if (project.hasLeftFile) 1 else 0) + (if (project.hasRightFile) 1 else 0)
    val progress = uploadedCount / 2f

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainer.copy(alpha = 0.5f),
        tonalElevation = 0.dp,
    ) {
        Row(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(AppSpacing.md),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = accentContainer.copy(alpha = 0.2f),
                        ) {
                            Icon(
                                Icons.Default.CompareArrows,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.padding(10.dp),
                            )
                        }
                        Column(modifier = Modifier.padding(start = AppSpacing.sm)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                            ) {
                                Text(
                                    project.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                CompareStatusTag(ready = ready, uploadedCount = uploadedCount)
                            }
                            Text(
                                formatModifiedAgo(project.modifiedAtEpochMs),
                                style = AppCodeSmallTextStyle,
                                color = colors.outline,
                            )
                        }
                    }
                    Text(
                        "${(progress * 100).toInt()}%",
                        style = AppLabelCapsTextStyle.copy(fontWeight = FontWeight.Black),
                        color = accent,
                        modifier =
                            Modifier
                                .background(accentContainer.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
                Spacer(Modifier.height(AppSpacing.md))
                AppThinProgress(progress = progress.coerceIn(0f, 1f), inProgress = !ready)
                Spacer(Modifier.height(AppSpacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        when {
                            ready ->
                                stringResource(
                                    Res.string.compare_project_files_ready,
                                    project.leftLangLabel,
                                    project.rightLangLabel,
                                )
                            uploadedCount > 0 ->
                                stringResource(Res.string.compare_project_upload_progress, uploadedCount)
                            else -> stringResource(Res.string.compare_project_status_pending)
                        },
                        style = AppCodeSmallTextStyle,
                        color = colors.onSurfaceVariant.copy(alpha = 0.85f),
                    )
                    Icon(
                        imageVector =
                            if (ready) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.AutoMirrored.Filled.ArrowForward
                            },
                        contentDescription = null,
                        tint = if (ready) colors.tertiary else colors.outline,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompareStatusTag(
    ready: Boolean,
    uploadedCount: Int,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val (label, container, content) =
        when {
            ready ->
                Triple(
                    stringResource(Res.string.compare_project_status_ready),
                    colors.tertiaryContainer.copy(alpha = 0.35f),
                    colors.onTertiaryContainer,
                )
            uploadedCount > 0 ->
                Triple(
                    stringResource(Res.string.compare_project_status_partial),
                    colors.primaryContainer.copy(alpha = 0.35f),
                    colors.onPrimaryContainer,
                )
            else ->
                Triple(
                    stringResource(Res.string.compare_project_status_pending),
                    colors.surfaceContainerHigh.copy(alpha = 0.35f),
                    colors.onSurfaceVariant,
                )
        }
    Text(
        text = label,
        style = AppLabelCapsTextStyle,
        color = content,
        modifier =
            modifier
                .background(container, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
private fun formatModifiedAgo(epochMs: Long): String {
    if (epochMs <= 0L) return stringResource(Res.string.file_project_modified_just_now)
    val hours = ((currentEpochMillis() - epochMs) / 3_600_000).coerceAtLeast(0)
    return when {
        hours < 1 -> stringResource(Res.string.file_project_modified_just_now)
        hours < 24 -> stringResource(Res.string.file_project_modified_hours, hours)
        else -> stringResource(Res.string.file_project_modified_days, hours / 24)
    }
}
