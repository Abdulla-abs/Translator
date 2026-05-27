package `fun`.abbas.android_res_translator.ui.screens.resmulti

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.theme.AppTheme
import `fun`.abbas.android_res_translator.persistence.ResMultiVersionIndex
import `fun`.abbas.android_res_translator.ui.components.UploadXmlCard
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.resmulti_init_failed
import androidrestranslator.composeapp.generated.resources.resmulti_init_in_progress
import androidrestranslator.composeapp.generated.resources.resmulti_init_pick_hint
import androidrestranslator.composeapp.generated.resources.resmulti_init_pick_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ResMultiFilesTab(
    project: ResMultiProject,
    versionIndex: ResMultiVersionIndex?,
    exportInProgress: Boolean,
    versionBusy: Boolean,
    onPickDirectory: () -> Unit,
    onExportAll: () -> Unit,
    onExportSingle: () -> Unit,
    onExportSingleXml: () -> Unit,
    onImportCompare: () -> Unit,
    onRestoreVersion: (String) -> Unit,
    onDeleteVersion: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        when (project.initState) {
            ResMultiInitState.INITIALIZING -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    CircularProgressIndicator()
                    Text(
                        stringResource(Res.string.resmulti_init_in_progress),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            ResMultiInitState.READY -> {
                ResMultiProjectInfoCard(project = project, modifier = Modifier.fillMaxWidth())
                ResMultiProjectFunctionsBento(
                    project = project,
                    actionsEnabled = !exportInProgress && !versionBusy,
                    onExportAll = onExportAll,
                    onExportSingle = onExportSingle,
                    onExportSingleXml = onExportSingleXml,
                    onImportCompare = onImportCompare,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (exportInProgress || versionBusy) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        CircularProgressIndicator()
                        Text(
                            stringResource(Res.string.resmulti_init_in_progress),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (versionIndex != null) {
                    ResMultiProjectVersionSection(
                        project = project,
                        versionIndex = versionIndex,
                        onRestoreVersion = onRestoreVersion,
                        onDeleteVersion = onDeleteVersion,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            ResMultiInitState.FAILED -> {
                UploadXmlCard(
                    titleRes = Res.string.resmulti_init_pick_title,
                    hintRes = Res.string.resmulti_init_pick_hint,
                    onClick = onPickDirectory,
                    onDrop = {},
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    stringResource(Res.string.resmulti_init_failed, project.initError.orEmpty()),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            ResMultiInitState.PENDING -> {
                UploadXmlCard(
                    titleRes = Res.string.resmulti_init_pick_title,
                    hintRes = Res.string.resmulti_init_pick_hint,
                    onClick = onPickDirectory,
                    onDrop = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Spacer(Modifier.height(AppSpacing.lg))
    }
}

@Preview()
@Composable
private fun ResMultiFilesTabReadyPreview() {
    AppTheme {
        ResMultiFilesTab(
            project = previewResMultiProject(),
            versionIndex = previewResMultiVersionIndex(),
            exportInProgress = false,
            versionBusy = false,
            onPickDirectory = {},
            onExportAll = {},
            onExportSingle = {},
            onExportSingleXml = {},
            onImportCompare = {},
            onRestoreVersion = {},
            onDeleteVersion = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun ResMultiFilesTabDirtyPreview() {
    AppTheme {
        ResMultiFilesTab(
            project = previewResMultiProject(dirty = true),
            versionIndex = previewResMultiVersionIndex(dirty = true),
            exportInProgress = false,
            versionBusy = false,
            onPickDirectory = {},
            onExportAll = {},
            onExportSingle = {},
            onExportSingleXml = {},
            onImportCompare = {},
            onRestoreVersion = {},
            onDeleteVersion = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun ResMultiFilesTabPendingPreview() {
    AppTheme {
        ResMultiFilesTab(
            project = previewResMultiProject(initState = ResMultiInitState.PENDING),
            versionIndex = null,
            exportInProgress = false,
            versionBusy = false,
            onPickDirectory = {},
            onExportAll = {},
            onExportSingle = {},
            onExportSingleXml = {},
            onImportCompare = {},
            onRestoreVersion = {},
            onDeleteVersion = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun ResMultiFilesTabFailedPreview() {
    AppTheme {
        ResMultiFilesTab(
            project =
                previewResMultiProject(
                    initState = ResMultiInitState.FAILED,
                    initError = "Permission denied reading res folder",
                ),
            versionIndex = null,
            exportInProgress = false,
            versionBusy = false,
            onPickDirectory = {},
            onExportAll = {},
            onExportSingle = {},
            onExportSingleXml = {},
            onImportCompare = {},
            onRestoreVersion = {},
            onDeleteVersion = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
