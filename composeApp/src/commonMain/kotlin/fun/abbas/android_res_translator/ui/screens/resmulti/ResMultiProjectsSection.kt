package `fun`.abbas.android_res_translator.ui.screens.resmulti

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.components.ResMultiProjectCard
import `fun`.abbas.android_res_translator.ui.components.UploadXmlCard
import `fun`.abbas.android_res_translator.ui.theme.AppControlShape
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.common_cancel
import androidrestranslator.composeapp.generated.resources.common_confirm
import androidrestranslator.composeapp.generated.resources.common_delete
import androidrestranslator.composeapp.generated.resources.resmulti_create_card_hint
import androidrestranslator.composeapp.generated.resources.resmulti_create_card_title
import androidrestranslator.composeapp.generated.resources.resmulti_create_dialog_title
import androidrestranslator.composeapp.generated.resources.resmulti_delete_message
import androidrestranslator.composeapp.generated.resources.resmulti_delete_title
import androidrestranslator.composeapp.generated.resources.resmulti_projects_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ResMultiProjectsSection(
    repository: ResMultiProjectRepository,
    onProjectClick: (ResMultiProject) -> Unit,
    onCreateProject: (String) -> Unit,
    onDeleteProject: (ResMultiProject) -> Unit,
    modifier: Modifier = Modifier,
) {
    val projects by repository.projects.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf("") }
    var projectPendingDelete by remember { mutableStateOf<ResMultiProject?>(null) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.FolderCopy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(22.dp),
            )
            Text(
                stringResource(Res.string.resmulti_projects_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = AppSpacing.sm),
            )
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val columns =
                when {
                    maxWidth >= 840.dp -> 3
                    maxWidth >= 600.dp -> 2
                    else -> 1
                }
            val cells = projects.map { ResMultiCell.Project(it) } + ResMultiCell.Create
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                cells.chunked(columns).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        row.forEach { cell ->
                            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                                when (cell) {
                                    is ResMultiCell.Project ->
                                        ResMultiProjectCard(
                                            project = cell.project,
                                            onClick = { onProjectClick(cell.project) },
                                            onLongClick = { projectPendingDelete = cell.project },
                                        )
                                    ResMultiCell.Create ->
                                        UploadXmlCard(
                                            titleRes = Res.string.resmulti_create_card_title,
                                            hintRes = Res.string.resmulti_create_card_hint,
                                            onClick = { showCreateDialog = true },
                                            onDrop = {},
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

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                projectName = ""
            },
            title = { Text(stringResource(Res.string.resmulti_create_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    singleLine = true,
                    shape = AppControlShape,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = projectName.trim()
                        if (name.isNotEmpty()) {
                            onCreateProject(name)
                            showCreateDialog = false
                            projectName = ""
                        }
                    },
                    enabled = projectName.trim().isNotEmpty(),
                ) {
                    Text(stringResource(Res.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateDialog = false
                        projectName = ""
                    },
                ) {
                    Text(stringResource(Res.string.common_cancel))
                }
            },
        )
    }

    projectPendingDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { projectPendingDelete = null },
            title = { Text(stringResource(Res.string.resmulti_delete_title)) },
            text = { Text(stringResource(Res.string.resmulti_delete_message, project.displayName)) },
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

private sealed interface ResMultiCell {
    data class Project(val project: ResMultiProject) : ResMultiCell

    data object Create : ResMultiCell
}
