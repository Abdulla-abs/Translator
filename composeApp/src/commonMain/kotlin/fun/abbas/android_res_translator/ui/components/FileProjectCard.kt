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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.screens.main.RecentXmlProject
import `fun`.abbas.android_res_translator.util.currentEpochMillis
import `fun`.abbas.android_res_translator.ui.theme.AppCodeSmallTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.file_project_keys_translated
import androidrestranslator.composeapp.generated.resources.file_project_modified_days
import androidrestranslator.composeapp.generated.resources.file_project_modified_hours
import androidrestranslator.composeapp.generated.resources.file_project_modified_just_now
import org.jetbrains.compose.resources.stringResource

@Composable
fun FileProjectCard(
    project: RecentXmlProject,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val inProgress = !project.isComplete
    val accent = if (inProgress) colors.primary else colors.secondary
    val accentContainer = if (inProgress) colors.primaryContainer else colors.secondaryContainer
    val percentLabel = "${(project.progressPercent * 100).toInt()}%"

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
                    .background(if (inProgress) colors.primaryContainer else colors.secondary),
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
                                Icons.Default.Code,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.padding(10.dp),
                            )
                        }
                        Column(modifier = Modifier.padding(start = AppSpacing.sm)) {
                            Text(
                                project.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                formatModifiedAgo(project.modifiedAtEpochMs),
                                style = AppCodeSmallTextStyle,
                                color = colors.outline,
                            )
                        }
                    }
                    Text(
                        percentLabel,
                        style = AppLabelCapsTextStyle.copy(fontWeight = FontWeight.Black),
                        color = if (inProgress) colors.primary else colors.secondary,
                        modifier =
                            Modifier
                                .background(
                                    if (inProgress) {
                                        colors.primaryContainer.copy(alpha = 0.2f)
                                    } else {
                                        colors.secondaryContainer.copy(alpha = 0.2f)
                                    },
                                    RoundedCornerShape(999.dp),
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
                Spacer(Modifier.height(AppSpacing.md))
                AppThinProgress(
                    progress = project.progressPercent.coerceIn(0f, 1f),
                    inProgress = inProgress,
                )
                Spacer(Modifier.height(AppSpacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(
                            Res.string.file_project_keys_translated,
                            project.translatedKeys,
                            project.totalKeys,
                        ),
                        style = AppCodeSmallTextStyle,
                        color = colors.onSurfaceVariant.copy(alpha = 0.85f),
                    )
                    Icon(
                        imageVector =
                            if (project.isComplete) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.AutoMirrored.Filled.ArrowForward
                            },
                        contentDescription = null,
                        tint = if (project.isComplete) colors.secondary else colors.outline,
                    )
                }
            }
        }
    }
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
