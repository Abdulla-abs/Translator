package `fun`.abbas.android_res_translator.ui.screens.resmulti


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.BoxWithConstraints

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.ExperimentalLayoutApi

import androidx.compose.foundation.layout.FlowRow

import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.size

import androidx.compose.foundation.layout.width

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight

import androidx.compose.material.icons.filled.Code

import androidx.compose.material.icons.filled.Language

import androidx.compose.material.icons.filled.SwapHoriz

import androidx.compose.material.icons.filled.TableChart

import androidx.compose.material.icons.filled.Translate

import androidx.compose.material3.Icon

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Surface

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp

import `fun`.abbas.android_res_translator.ui.theme.AppCodeSmallTextStyle

import `fun`.abbas.android_res_translator.ui.theme.AppControlShape

import `fun`.abbas.android_res_translator.ui.theme.AppIndicatorShape

import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle

import `fun`.abbas.android_res_translator.ui.theme.AppOutlineStrokeWidth

import `fun`.abbas.android_res_translator.ui.theme.AppSpacing

import `fun`.abbas.android_res_translator.util.currentEpochMillis

import androidrestranslator.composeapp.generated.resources.Res

import androidrestranslator.composeapp.generated.resources.file_project_modified_days

import androidrestranslator.composeapp.generated.resources.file_project_modified_hours

import androidrestranslator.composeapp.generated.resources.file_project_modified_just_now

import androidrestranslator.composeapp.generated.resources.resmulti_detail_import_compare

import androidrestranslator.composeapp.generated.resources.resmulti_detail_imported_locales

import androidrestranslator.composeapp.generated.resources.resmulti_detail_source_path_label

import androidrestranslator.composeapp.generated.resources.resmulti_export_all_xlsx

import androidrestranslator.composeapp.generated.resources.resmulti_export_single_xml
import androidrestranslator.composeapp.generated.resources.resmulti_export_single_xlsx

import androidrestranslator.composeapp.generated.resources.resmulti_files_core_operations

import androidrestranslator.composeapp.generated.resources.resmulti_files_export_full_desc

import androidrestranslator.composeapp.generated.resources.resmulti_files_export_single_desc
import androidrestranslator.composeapp.generated.resources.resmulti_files_export_single_xml_desc

import androidrestranslator.composeapp.generated.resources.resmulti_files_import_compare_desc

import androidrestranslator.composeapp.generated.resources.resmulti_files_metadata_title

import org.jetbrains.compose.resources.stringResource

import androidx.compose.ui.tooling.preview.Preview

import `fun`.abbas.android_res_translator.ui.theme.AppTheme


@OptIn(ExperimentalLayoutApi::class)

@Composable

fun ResMultiProjectInfoCard(

    project: ResMultiProject,

    modifier: Modifier = Modifier,

    ) {

    val colors = MaterialTheme.colorScheme

    ResMultiSectionCard(modifier = modifier) {

        Row(

            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement = Arrangement.SpaceBetween,

            verticalAlignment = Alignment.Top,

            ) {

            Column(modifier = Modifier.weight(1f)) {

                Text(

                    stringResource(Res.string.resmulti_files_metadata_title).uppercase(),

                    style = AppLabelCapsTextStyle,

                    color = colors.onSurfaceVariant,

                    )

                Text(

                    project.displayName,

                    style = MaterialTheme.typography.headlineSmall,

                    fontWeight = FontWeight.Bold,

                    modifier = Modifier.padding(top = AppSpacing.xs),

                    )

            }

            Column(horizontalAlignment = Alignment.End) {

                Text(

                    formatModifiedAgo(project.modifiedAtEpochMs),

                    style = MaterialTheme.typography.bodySmall,

                    color = colors.onSurfaceVariant,

                    )

                Spacer(Modifier.height(AppSpacing.xs))

                ResMultiProjectMetadataAccent()

            }

        }



        if (project.sourceResPath.isNotBlank()) {

            Spacer(Modifier.height(AppSpacing.md))

            ResMultiProjectSourcePathTag(path = project.sourceResPath)

        }



        Text(

            stringResource(Res.string.resmulti_detail_imported_locales, project.languages.size),

            style = MaterialTheme.typography.labelMedium,

            color = colors.onSurfaceVariant,

            modifier =

                Modifier.padding(

                    top = if (project.sourceResPath.isNotBlank()) AppSpacing.sm else AppSpacing.md,

                ),

            )

        FlowRow(

            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),

            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),

            modifier = Modifier.padding(top = AppSpacing.sm),

            ) {

            project.languages.forEach { lang ->

                ResMultiProjectLocaleChip(lang = lang)

            }

        }

    }

}


@Composable

private fun ResMultiProjectMetadataAccent() {

    val colors = MaterialTheme.colorScheme

    Row(

        verticalAlignment = Alignment.CenterVertically,

        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),

        ) {

        Box(

            modifier =

                Modifier

                    .width(28.dp)

                    .height(3.dp)

                    .background(colors.primaryContainer, AppIndicatorShape),

            )

        Box(

            modifier =

                Modifier

                    .size(4.dp)

                    .background(colors.outline, CircleShape),

            )

    }

}


@Composable
private fun ResMultiProjectSourcePathTag(path: String) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(
            stringResource(Res.string.resmulti_detail_source_path_label),
            style = AppLabelCapsTextStyle,
            color = colors.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = path,
                style = AppCodeSmallTextStyle,
                color = colors.primary,
                modifier =
                    Modifier
                        .border(
                            width = 1.dp,
                            color = colors.primaryContainer.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(6.dp),
                        )
                        .background(
                            colors.primaryContainer.copy(alpha = 0.15f),
                            RoundedCornerShape(6.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }
}


@Composable

private fun ResMultiProjectLocaleChip(lang: ResMultiLanguageEntry) {

    val colors = MaterialTheme.colorScheme

    Surface(

        shape = RoundedCornerShape(50),

        color = colors.surfaceContainerHigh,

        border = BorderStroke(AppOutlineStrokeWidth.dp, colors.outlineVariant),

        ) {

        Row(

            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),

            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement = Arrangement.spacedBy(6.dp),

            ) {

            Icon(

                Icons.Default.Language,

                contentDescription = null,

                modifier = Modifier.size(14.dp),

                tint = colors.primary,

                )

            Text(

                "${lang.langCode} · ${lang.folderName}",

                style = AppCodeSmallTextStyle,

                color = colors.primary,

                )

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


@Composable

fun ResMultiProjectFunctionsBento(

    project: ResMultiProject,

    onExportAll: () -> Unit,

    onExportSingle: () -> Unit,

    onExportSingleXml: () -> Unit,

    onImportCompare: () -> Unit,

    actionsEnabled: Boolean,

    modifier: Modifier = Modifier,

    ) {

    val canAct = actionsEnabled && project.languages.isNotEmpty()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {

        Text(

            stringResource(Res.string.resmulti_files_core_operations).uppercase(),

            style = AppLabelCapsTextStyle,

            color = MaterialTheme.colorScheme.onSurfaceVariant,

            )

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {

            val twoColumns = maxWidth >= 600.dp

            if (twoColumns) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    verticalAlignment = Alignment.Top,
                    ) {

                    ResMultiBentoActionCard(

                        title = stringResource(Res.string.resmulti_export_all_xlsx),

                        description = stringResource(Res.string.resmulti_files_export_full_desc),

                        icon = Icons.Default.TableChart,

                        iconBackground = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),

                        enabled = canAct,

                        onClick = onExportAll,

                        modifier = Modifier.weight(1f),

                        )

                    ResMultiBentoActionCard(

                        title = stringResource(Res.string.resmulti_export_single_xlsx),

                        description = stringResource(Res.string.resmulti_files_export_single_desc),

                        icon = Icons.Default.Translate,

                        iconBackground = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),

                        enabled = canAct,

                        onClick = onExportSingle,

                        modifier = Modifier.weight(1f),

                        )

                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    verticalAlignment = Alignment.Top,
                ) {
                    ResMultiBentoActionCard(
                        title = stringResource(Res.string.resmulti_export_single_xml),
                        description = stringResource(Res.string.resmulti_files_export_single_xml_desc),
                        icon = Icons.Default.Code,
                        iconBackground = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
                        enabled = canAct,
                        onClick = onExportSingleXml,
                        modifier = if (twoColumns) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                    )
                }

            } else {

                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {

                    ResMultiBentoActionCard(

                        title = stringResource(Res.string.resmulti_export_all_xlsx),

                        description = stringResource(Res.string.resmulti_files_export_full_desc),

                        icon = Icons.Default.TableChart,

                        iconBackground = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),

                        enabled = canAct,

                        onClick = onExportAll,

                        )

                    ResMultiBentoActionCard(

                        title = stringResource(Res.string.resmulti_export_single_xlsx),

                        description = stringResource(Res.string.resmulti_files_export_single_desc),

                        icon = Icons.Default.Translate,

                        iconBackground = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),

                        enabled = canAct,

                        onClick = onExportSingle,

                        )

                    ResMultiBentoActionCard(
                        title = stringResource(Res.string.resmulti_export_single_xml),
                        description = stringResource(Res.string.resmulti_files_export_single_xml_desc),
                        icon = Icons.Default.Code,
                        iconBackground = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
                        enabled = canAct,
                        onClick = onExportSingleXml,
                    )

                }

            }

        }

        ResMultiBentoWideActionCard(

            title = stringResource(Res.string.resmulti_detail_import_compare),

            description = stringResource(Res.string.resmulti_files_import_compare_desc),

            icon = Icons.Default.SwapHoriz,

            iconBackground = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),

            enabled = canAct,

            onClick = onImportCompare,

            )

    }

}


@Composable

private fun ResMultiBentoActionCard(

    title: String,

    description: String,

    icon: androidx.compose.ui.graphics.vector.ImageVector,

    iconBackground: androidx.compose.ui.graphics.Color,

    enabled: Boolean,

    onClick: () -> Unit,

    modifier: Modifier = Modifier,

    ) {

    ResMultiSectionCard(

        modifier =

            modifier.clickable(enabled = enabled, onClick = onClick),

        ) {

        Surface(

            shape = AppControlShape,

            color = iconBackground,

            ) {

            Icon(

                icon,

                contentDescription = null,

                modifier = Modifier.padding(10.dp).size(24.dp),

                tint = MaterialTheme.colorScheme.primary,

                )

        }

        Text(

            title,

            style = MaterialTheme.typography.titleMedium,

            fontWeight = FontWeight.SemiBold,

            modifier = Modifier.padding(top = AppSpacing.md),

            )

        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AppSpacing.xs),
            )
    }
}


@Composable

private fun ResMultiBentoWideActionCard(

    title: String,

    description: String,

    icon: androidx.compose.ui.graphics.vector.ImageVector,

    iconBackground: androidx.compose.ui.graphics.Color,

    enabled: Boolean,

    onClick: () -> Unit,

    ) {

    ResMultiSectionCard(

        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),

        ) {

        Row(

            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),

            ) {

            Surface(

                shape = AppControlShape,

                color = iconBackground,

                ) {

                Icon(

                    icon,

                    contentDescription = null,

                    modifier = Modifier.padding(12.dp).size(32.dp),

                    tint = MaterialTheme.colorScheme.tertiary,

                    )

            }

            Column(modifier = Modifier.weight(1f)) {

                Text(

                    title,

                    style = MaterialTheme.typography.titleMedium,

                    fontWeight = FontWeight.Bold,

                    )

                Text(

                    description,

                    style = MaterialTheme.typography.bodySmall,

                    color = MaterialTheme.colorScheme.onSurfaceVariant,

                    modifier = Modifier.padding(top = AppSpacing.xs),

                    )

            }

            Icon(

                Icons.AutoMirrored.Filled.KeyboardArrowRight,

                contentDescription = null,

                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),

                )

        }

    }

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


@Preview

@Composable

private fun ResMultiProjectInfoCardPreview() {

    AppTheme {

        ResMultiProjectInfoCard(project = previewResMultiProject())

    }

}


@Preview

@Composable

private fun ResMultiProjectFunctionsBentoPreview() {

    AppTheme {

        ResMultiProjectFunctionsBento(

            project = previewResMultiProject(),

            onExportAll = {},

            onExportSingle = {},

            onExportSingleXml = {},

            onImportCompare = {},

            actionsEnabled = true,

            )

    }

}


