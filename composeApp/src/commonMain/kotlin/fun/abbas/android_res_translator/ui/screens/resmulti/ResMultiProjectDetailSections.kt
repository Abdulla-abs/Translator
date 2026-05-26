package `fun`.abbas.android_res_translator.ui.screens.resmulti

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.persistence.ResMultiVersionIndex
import `fun`.abbas.android_res_translator.ui.components.AppSectionCard
import `fun`.abbas.android_res_translator.ui.components.AppSectionTitle
import `fun`.abbas.android_res_translator.ui.theme.AppCodeSmallTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppControlShape
import `fun`.abbas.android_res_translator.ui.theme.AppOutlineStrokeWidth
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import `fun`.abbas.android_res_translator.util.currentEpochMillis
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.file_project_modified_days
import androidrestranslator.composeapp.generated.resources.file_project_modified_hours
import androidrestranslator.composeapp.generated.resources.file_project_modified_just_now
import androidrestranslator.composeapp.generated.resources.resmulti_detail_created_at
import androidrestranslator.composeapp.generated.resources.resmulti_detail_feature_coming_soon
import androidrestranslator.composeapp.generated.resources.resmulti_detail_functions_title
import androidrestranslator.composeapp.generated.resources.resmulti_detail_import_compare
import androidrestranslator.composeapp.generated.resources.resmulti_detail_info_title
import androidrestranslator.composeapp.generated.resources.resmulti_detail_language_count
import androidrestranslator.composeapp.generated.resources.resmulti_detail_languages_label
import androidrestranslator.composeapp.generated.resources.resmulti_detail_project_name
import androidrestranslator.composeapp.generated.resources.resmulti_detail_source_path
import androidrestranslator.composeapp.generated.resources.resmulti_detail_version_count
import androidrestranslator.composeapp.generated.resources.resmulti_detail_version_list
import androidrestranslator.composeapp.generated.resources.resmulti_detail_version_push
import androidrestranslator.composeapp.generated.resources.resmulti_detail_versions_title
import androidrestranslator.composeapp.generated.resources.resmulti_export_all_xlsx
import androidrestranslator.composeapp.generated.resources.resmulti_export_single_xlsx
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResMultiProjectInfoCard(
    project: ResMultiProject,
    modifier: Modifier = Modifier,
) {
    AppSectionCard(modifier = modifier) {
        AppSectionTitle(stringResource(Res.string.resmulti_detail_info_title))
        Text(
            stringResource(Res.string.resmulti_detail_project_name, project.displayName),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(
                Res.string.resmulti_detail_created_at,
                formatEpochAgo(project.createdAtEpochMs),
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AppSpacing.xs),
        )
        if (project.sourceResPath.isNotBlank()) {
            Text(
                stringResource(Res.string.resmulti_detail_source_path, project.sourceResPath),
                style = AppCodeSmallTextStyle,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = AppSpacing.xs),
            )
        }
        Text(
            stringResource(Res.string.resmulti_detail_language_count, project.languages.size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AppSpacing.sm),
        )
        Text(
            stringResource(Res.string.resmulti_detail_languages_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = AppSpacing.sm),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            modifier = Modifier.padding(top = AppSpacing.xs),
        ) {
            project.languages.forEach { lang ->
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                ) {
                    Text(
                        "${lang.langCode} · ${lang.folderName}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun ResMultiProjectFunctionsSection(
    project: ResMultiProject,
    onExportAll: () -> Unit,
    onExportSingle: () -> Unit,
    onImportCompare: () -> Unit,
    actionsEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val canAct = actionsEnabled && project.languages.isNotEmpty()
    AppSectionCard(modifier = modifier) {
        AppSectionTitle(stringResource(Res.string.resmulti_detail_functions_title))
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            ResMultiActionButton(
                label = stringResource(Res.string.resmulti_export_all_xlsx),
                icon = Icons.Default.TableChart,
                enabled = canAct,
                onClick = onExportAll,
            )
            ResMultiActionButton(
                label = stringResource(Res.string.resmulti_export_single_xlsx),
                icon = Icons.Default.TableView,
                enabled = canAct,
                onClick = onExportSingle,
            )
            ResMultiActionButton(
                label = stringResource(Res.string.resmulti_detail_import_compare),
                icon = Icons.Default.UploadFile,
                enabled = canAct,
                onClick = onImportCompare,
            )
        }
    }
}

@Composable
internal fun ResMultiActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = AppControlShape,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Text(label, modifier = Modifier.padding(start = AppSpacing.sm))
    }
}

@Composable
private fun ResMultiPlaceholderActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    ResMultiActionButton(
        label = label,
        icon = icon,
        enabled = false,
        onClick = {},
        modifier = modifier,
    )
}

@Composable
internal fun formatEpochAgo(epochMs: Long): String {
    if (epochMs <= 0L) return stringResource(Res.string.file_project_modified_just_now)
    val hours = ((currentEpochMillis() - epochMs) / 3_600_000).coerceAtLeast(0)
    return when {
        hours < 1 -> stringResource(Res.string.file_project_modified_just_now)
        hours < 24 -> stringResource(Res.string.file_project_modified_hours, hours)
        else -> stringResource(Res.string.file_project_modified_days, hours / 24)
    }
}
