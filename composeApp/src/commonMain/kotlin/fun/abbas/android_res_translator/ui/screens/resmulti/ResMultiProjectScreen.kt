package `fun`.abbas.android_res_translator.ui.screens.resmulti

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import `fun`.abbas.android_res_translator.persistence.ResMultiVersionIndex
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import `fun`.abbas.android_res_translator.core.resources.export.StringsMatrix
import `fun`.abbas.android_res_translator.core.resources.resmulti.ResMultiProjectExporter
import `fun`.abbas.android_res_translator.core.resources.resmulti.ResMultiProjectImportService
import `fun`.abbas.android_res_translator.core.resources.resmulti.ResMultiXlsxExport
import `fun`.abbas.android_res_translator.ui.rememberXmlFileAccess
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import `fun`.abbas.android_res_translator.persistence.ResMultiVersionIndexEntry
import `fun`.abbas.android_res_translator.persistence.ResProjectVersionStore
import `fun`.abbas.android_res_translator.ui.rememberDirectoryPicker
import `fun`.abbas.android_res_translator.ui.navigation.AppBackHandler
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.resmulti_export_failed
import androidrestranslator.composeapp.generated.resources.resmulti_import_parse_error
import androidrestranslator.composeapp.generated.resources.resmulti_version_operation_failed
import androidrestranslator.composeapp.generated.resources.resmulti_version_push_success
import androidrestranslator.composeapp.generated.resources.resmulti_export_no_languages
import androidrestranslator.composeapp.generated.resources.resmulti_export_pick_language_title
import androidrestranslator.composeapp.generated.resources.file_editor_export_cancelled
import androidrestranslator.composeapp.generated.resources.file_editor_exported
import androidrestranslator.composeapp.generated.resources.common_cancel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun ResMultiProjectScreen(
    project: ResMultiProject,
    repository: ResMultiProjectRepository,
    onBack: () -> Unit,
    onNavigateToImportCompare: (StringsMatrix) -> Unit,
    modifier: Modifier = Modifier,
) {
    val projects by repository.projects.collectAsState()
    val current = projects.find { it.id == project.id } ?: project
    val scope = rememberCoroutineScope()
    val pickDirectory = rememberDirectoryPicker { path ->
        if (path != null) {
            scope.launch {
                repository.initializeFromSource(current.id, path)
            }
        }
    }
    var versionRefreshKey by remember { mutableIntStateOf(0) }
    val versionIndex: ResMultiVersionIndex? =
        remember(current.id, current.initState, current.dirty, versionRefreshKey) {
            if (current.isReady) {
                ResProjectVersionStore.readVersionIndex(current.id)
            } else {
                null
            }
        }
    var pendingRestoreVersion by remember { mutableStateOf<ResMultiVersionIndexEntry?>(null) }
    var pendingDeleteVersion by remember { mutableStateOf<ResMultiVersionIndexEntry?>(null) }
    var versionBusy by remember { mutableStateOf(false) }
    val versionPushSuccessMessage = stringResource(Res.string.resmulti_version_push_success)
    val xmlFileAccess = rememberXmlFileAccess()
    val snackbarHostState = remember { SnackbarHostState() }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var exportInProgress by remember { mutableStateOf(false) }
    var showPushVersionDialog by remember { mutableStateOf(false) }
    val canPushVersion =
        current.isReady &&
            versionIndex != null &&
            (current.dirty || versionIndex.dirty) &&
            !versionBusy
    val exportedMessage = stringResource(Res.string.file_editor_exported)
    val exportCancelledMessage = stringResource(Res.string.file_editor_export_cancelled)
    val noLanguagesMessage = stringResource(Res.string.resmulti_export_no_languages)

    fun saveSpreadsheet(result: Result<ResMultiXlsxExport>) {
        result
            .onSuccess { export ->
                xmlFileAccess.launchSaveSpreadsheet(export.bytes, export.suggestedFileName) { ok ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (ok) exportedMessage else exportCancelledMessage,
                        )
                    }
                }
            }.onFailure { error ->
                val detail = error.message ?: error.toString()
                scope.launch {
                    snackbarHostState.showSnackbar(
                        getString(Res.string.resmulti_export_failed, detail),
                    )
                }
            }
    }

    fun performExportAll() {
        if (current.languages.isEmpty()) {
            scope.launch { snackbarHostState.showSnackbar(noLanguagesMessage) }
            return
        }
        exportInProgress = true
        scope.launch {
            val result =
                withContext(Dispatchers.Default) {
                    ResMultiProjectExporter.exportFull(current)
                }
            exportInProgress = false
            saveSpreadsheet(result)
        }
    }

    fun refreshVersions() {
        versionRefreshKey++
    }

    fun pushVersion(name: String) {
        versionBusy = true
        scope.launch {
            val result = repository.pushVersion(current.id, name)
            versionBusy = false
            result
                .onSuccess {
                    refreshVersions()
                    snackbarHostState.showSnackbar(versionPushSuccessMessage)
                }.onFailure { error ->
                    snackbarHostState.showSnackbar(
                        getString(
                            Res.string.resmulti_version_operation_failed,
                            error.message ?: error.toString(),
                        ),
                    )
                }
        }
    }

    fun restoreVersion(versionId: String) {
        versionBusy = true
        scope.launch {
            val result = repository.restoreToVersion(current.id, versionId)
            versionBusy = false
            result
                .onSuccess { refreshVersions() }
                .onFailure { error ->
                    snackbarHostState.showSnackbar(
                        getString(
                            Res.string.resmulti_version_operation_failed,
                            error.message ?: error.toString(),
                        ),
                    )
                }
        }
    }

    fun deleteVersion(versionId: String) {
        versionBusy = true
        scope.launch {
            val result = repository.deleteVersionAndSuccessors(current.id, versionId)
            versionBusy = false
            result
                .onSuccess { refreshVersions() }
                .onFailure { error ->
                    snackbarHostState.showSnackbar(
                        getString(
                            Res.string.resmulti_version_operation_failed,
                            error.message ?: error.toString(),
                        ),
                    )
                }
        }
    }

    fun startImportCompare() {
        xmlFileAccess.launchPickSpreadsheet { result ->
            result
                .onSuccess { bytes ->
                    scope.launch {
                        val decoded =
                            withContext(Dispatchers.Default) {
                                ResMultiProjectImportService.decodeSpreadsheet(bytes)
                            }
                        decoded
                            .onSuccess { matrix -> onNavigateToImportCompare(matrix) }
                            .onFailure { error ->
                                snackbarHostState.showSnackbar(
                                    getString(
                                        Res.string.resmulti_import_parse_error,
                                        error.message ?: error.toString(),
                                    ),
                                )
                            }
                    }
                }.onFailure { error ->
                    if (error.message?.contains("取消") != true &&
                        error.message?.contains("Cancelled", ignoreCase = true) != true
                    ) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                getString(
                                    Res.string.resmulti_import_parse_error,
                                    error.message ?: error.toString(),
                                ),
                            )
                        }
                    }
                }
        }
    }

    fun performExportSingle(language: ResMultiLanguageEntry) {
        showLanguagePicker = false
        exportInProgress = true
        scope.launch {
            val result =
                withContext(Dispatchers.Default) {
                    ResMultiProjectExporter.exportSingle(current, language)
                }
            exportInProgress = false
            saveSpreadsheet(result)
        }
    }

    LaunchedEffect(project.id) {
        repository.reloadFromDisk()
    }

    AppBackHandler(onBack = onBack)

    if (showPushVersionDialog) {
        PushVersionDialog(
            onDismiss = { showPushVersionDialog = false },
            onConfirm = { name ->
                showPushVersionDialog = false
                pushVersion(name)
            },
        )
    }

    pendingRestoreVersion?.let { version ->
        RestoreVersionConfirmDialog(
            version = version,
            onDismiss = { pendingRestoreVersion = null },
            onConfirm = {
                pendingRestoreVersion = null
                restoreVersion(version.id)
            },
        )
    }

    pendingDeleteVersion?.let { version ->
        val index = versionIndex
        if (index != null) {
            DeleteVersionConfirmDialog(
                version = version,
                versionIndex = index,
                onDismiss = { pendingDeleteVersion = null },
                onConfirm = {
                    pendingDeleteVersion = null
                    deleteVersion(version.id)
                },
            )
        }
    }

    if (showLanguagePicker) {
        Dialog(onDismissRequest = { showLanguagePicker = false }) {
            Surface(shape = MaterialTheme.shapes.large) {
                Column(
                    modifier = Modifier.padding(AppSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Text(stringResource(Res.string.resmulti_export_pick_language_title))
                    current.languages.forEach { lang ->
                        Text(
                            "${lang.langCode} · ${lang.folderName}",
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { performExportSingle(lang) }
                                    .padding(vertical = AppSpacing.sm),
                        )
                    }
                    TextButton(
                        onClick = { showLanguagePicker = false },
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text(stringResource(Res.string.common_cancel))
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            ResMultiProjectHeader(
                projectName = current.displayName,
                onBackClick = onBack,
                showPushVersion = current.isReady && versionIndex != null,
                pushVersionEnabled = canPushVersion,
                onPushVersionClick = { showPushVersionDialog = true },
            )
            ResMultiFilesTab(
                project = current,
                versionIndex = versionIndex,
                exportInProgress = exportInProgress,
                versionBusy = versionBusy,
                onPickDirectory = pickDirectory,
                onExportAll = ::performExportAll,
                onExportSingle = {
                    if (current.languages.isEmpty()) {
                        scope.launch { snackbarHostState.showSnackbar(noLanguagesMessage) }
                    } else {
                        showLanguagePicker = true
                    }
                },
                onImportCompare = {
                    if (current.languages.isEmpty()) {
                        scope.launch { snackbarHostState.showSnackbar(noLanguagesMessage) }
                    } else {
                        startImportCompare()
                    }
                },
                onRestoreVersion = { versionId ->
                    pendingRestoreVersion = versionIndex?.versions?.find { it.id == versionId }
                },
                onDeleteVersion = { versionId ->
                    pendingDeleteVersion = versionIndex?.versions?.find { it.id == versionId }
                },
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.gutter),
            )
        }
    }
}
