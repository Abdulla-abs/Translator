package `fun`.abbas.android_res_translator.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.components.FileProjectCard
import `fun`.abbas.android_res_translator.ui.components.UploadXmlCard
import `fun`.abbas.android_res_translator.ui.files.DroppedXmlFile
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.common_cancel
import androidrestranslator.composeapp.generated.resources.common_delete
import androidrestranslator.composeapp.generated.resources.dashboard_file_projects_title
import androidrestranslator.composeapp.generated.resources.dashboard_upload_hint_full
import androidrestranslator.composeapp.generated.resources.dashboard_upload_hint_incremental
import androidrestranslator.composeapp.generated.resources.dashboard_upload_xml_full
import androidrestranslator.composeapp.generated.resources.dashboard_upload_xml_incremental
import androidrestranslator.composeapp.generated.resources.dashboard_view_all
import androidrestranslator.composeapp.generated.resources.file_projects_delete_message
import androidrestranslator.composeapp.generated.resources.file_projects_delete_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun FileProjectsSection(
    repository: TranslationProjectRepository,
    onViewAllClick: () -> Unit,
    onIncrementalUploadClick: () -> Unit,
    onFullUploadClick: () -> Unit,
    onIncrementalDrop: (List<DroppedXmlFile>) -> Unit,
    onFullDrop: (List<DroppedXmlFile>) -> Unit,
    onProjectClick: (RecentXmlProject) -> Unit,
    onDeleteProject: (RecentXmlProject) -> Unit,
    modifier: Modifier = Modifier,
) {
    val projects by repository.projects.collectAsState()
    var projectPendingDelete by remember { mutableStateOf<RecentXmlProject?>(null) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FolderZip, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
                Text(
                    stringResource(Res.string.dashboard_file_projects_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = AppSpacing.sm),
                )
            }
            Text(
                stringResource(Res.string.dashboard_view_all),
                style = AppLabelCapsTextStyle,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onViewAllClick),
            )
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val columns =
                when {
                    maxWidth >= 840.dp -> 3
                    maxWidth >= 600.dp -> 2
                    else -> 1
                }
            val cells =
                projects.map { Cell.Project(it) } + Cell.UploadFull + Cell.UploadIncremental
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                cells.chunked(columns).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        row.forEach { cell ->
                            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                                when (cell) {
                                    is Cell.Project ->
                                        FileProjectCard(
                                            project = cell.project,
                                            onClick = { onProjectClick(cell.project) },
                                            onLongClick = { projectPendingDelete = cell.project },
                                        )
                                    Cell.UploadFull ->
                                        UploadXmlCard(
                                            titleRes = Res.string.dashboard_upload_xml_full,
                                            hintRes = Res.string.dashboard_upload_hint_full,
                                            onClick = onFullUploadClick,
                                            onDrop = onFullDrop,
                                        )
                                    Cell.UploadIncremental ->
                                        UploadXmlCard(
                                            titleRes = Res.string.dashboard_upload_xml_incremental,
                                            hintRes = Res.string.dashboard_upload_hint_incremental,
                                            onClick = onIncrementalUploadClick,
                                            onDrop = onIncrementalDrop,
                                        )
                                }
                            }
                        }
                        repeat(columns - row.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    projectPendingDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { projectPendingDelete = null },
            title = { Text(stringResource(Res.string.file_projects_delete_title)) },
            text = {
                Text(
                    stringResource(Res.string.file_projects_delete_message, project.displayName),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProject(project)
                        projectPendingDelete = null
                    },
                ) {
                    Text(stringResource(Res.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { projectPendingDelete = null }) {
                    Text(stringResource(Res.string.common_cancel))
                }
            },
        )
    }
}

private sealed interface Cell {
    data class Project(val project: RecentXmlProject) : Cell
    data object UploadIncremental : Cell
    data object UploadFull : Cell
}
