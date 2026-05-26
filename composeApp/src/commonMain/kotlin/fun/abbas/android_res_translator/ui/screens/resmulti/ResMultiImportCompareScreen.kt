package `fun`.abbas.android_res_translator.ui.screens.resmulti

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.core.resources.export.StringsMatrix
import `fun`.abbas.android_res_translator.core.resources.resmulti.ResMultiImportApplier
import `fun`.abbas.android_res_translator.core.resources.resmulti.ResMultiImportCompareCell
import `fun`.abbas.android_res_translator.core.resources.resmulti.ResMultiImportCompareMatrix
import `fun`.abbas.android_res_translator.core.resources.resmulti.ResMultiImportCompareRow
import `fun`.abbas.android_res_translator.core.resources.resmulti.ResMultiProjectImportService
import `fun`.abbas.android_res_translator.ui.components.grid.StickyMultiCompareGrid
import `fun`.abbas.android_res_translator.ui.navigation.AppBackHandler
import `fun`.abbas.android_res_translator.ui.platform.rememberCopyToClipboardHandler
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.common_back
import androidrestranslator.composeapp.generated.resources.common_cancel
import androidrestranslator.composeapp.generated.resources.common_confirm
import androidrestranslator.composeapp.generated.resources.compare_cell_copied_snackbar
import androidrestranslator.composeapp.generated.resources.compare_detail_diff_count
import androidrestranslator.composeapp.generated.resources.resmulti_import_apply_confirm_message
import androidrestranslator.composeapp.generated.resources.resmulti_import_apply_confirm_title
import androidrestranslator.composeapp.generated.resources.resmulti_import_apply_menu
import androidrestranslator.composeapp.generated.resources.resmulti_import_apply_success
import androidrestranslator.composeapp.generated.resources.resmulti_import_cell_imported
import androidrestranslator.composeapp.generated.resources.resmulti_import_cell_workspace
import androidrestranslator.composeapp.generated.resources.resmulti_import_no_columns
import androidrestranslator.composeapp.generated.resources.resmulti_import_parse_error
import androidrestranslator.composeapp.generated.resources.resmulti_import_skipped_columns
import androidrestranslator.composeapp.generated.resources.resmulti_import_summary
import androidrestranslator.composeapp.generated.resources.resmulti_init_in_progress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun ResMultiImportCompareScreen(
    project: ResMultiProject,
    importedMatrix: StringsMatrix,
    repository: ResMultiProjectRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val compareResult =
        remember(project.id, importedMatrix) {
            ResMultiProjectImportService.buildCompareMatrix(project, importedMatrix)
        }
    val copyToClipboard = rememberCopyToClipboardHandler()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    var showApplyConfirm by remember { mutableStateOf(false) }
    var applyInProgress by remember { mutableStateOf(false) }
    var selectedDiff by remember { mutableStateOf<Triple<ResMultiImportCompareRow, Int, ResMultiImportCompareCell>?>(null) }

    fun copyCellWithSnackbar(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        copyToClipboard(trimmed)
        val preview = if (trimmed.length > 48) "${trimmed.take(48)}…" else trimmed
        scope.launch {
            snackbarHostState.showSnackbar(getString(Res.string.compare_cell_copied_snackbar, preview))
        }
    }

    AppBackHandler(onBack = onBack)

    selectedDiff?.let { (row, colIndex, cell) ->
        val columnLabel =
            compareResult.getOrNull()?.columns?.getOrNull(colIndex)?.headerLabel.orEmpty()
        AlertDialog(
            onDismissRequest = { selectedDiff = null },
            title = { Text("${row.key} · $columnLabel") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Text(
                        stringResource(Res.string.resmulti_import_cell_workspace, cell.workspaceValue.ifEmpty { "—" }),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        stringResource(Res.string.resmulti_import_cell_imported, cell.importValue.ifEmpty { "—" }),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedDiff = null }) {
                    Text(stringResource(Res.string.common_confirm))
                }
            },
        )
    }

    if (showApplyConfirm) {
        val matrix = compareResult.getOrNull()
        val diffCount = matrix?.diffCellCount ?: 0
        AlertDialog(
            onDismissRequest = { showApplyConfirm = false },
            title = { Text(stringResource(Res.string.resmulti_import_apply_confirm_title)) },
            text = {
                Text(stringResource(Res.string.resmulti_import_apply_confirm_message, diffCount))
            },
            confirmButton = {
                TextButton(
                    enabled = !applyInProgress && matrix != null && diffCount > 0,
                    onClick = {
                        if (matrix == null) return@TextButton
                        applyInProgress = true
                        scope.launch {
                            val result =
                                withContext(Dispatchers.Default) {
                                    ResMultiImportApplier.applyDiffs(project.id, matrix)
                                }
                            applyInProgress = false
                            showApplyConfirm = false
                            result
                                .onSuccess {
                                    repository.reloadFromDisk()
                                    snackbarHostState.showSnackbar(
                                        getString(
                                            Res.string.resmulti_import_apply_success,
                                            it.totalChangedCells,
                                        ),
                                    )
                                    onBack()
                                }.onFailure {
                                    snackbarHostState.showSnackbar(it.message ?: it.toString())
                                }
                        }
                    },
                ) {
                    Text(stringResource(Res.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyConfirm = false }) {
                    Text(stringResource(Res.string.common_cancel))
                }
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(AppSpacing.gutter)
                    .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back))
                }
                Text(
                    project.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = AppSpacing.sm).weight(1f),
                )
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.resmulti_import_apply_menu)) },
                            onClick = {
                                showMenu = false
                                showApplyConfirm = true
                            },
                            enabled = (compareResult.getOrNull()?.diffCellCount ?: 0) > 0 && !applyInProgress,
                        )
                    }
                }
            }
            if (applyInProgress) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = AppSpacing.sm))
                    Text(stringResource(Res.string.resmulti_init_in_progress))
                }
            }
            Spacer(Modifier.height(AppSpacing.md))
            compareResult.fold(
                onSuccess = { matrix: ResMultiImportCompareMatrix ->
                    if (matrix.columns.isEmpty()) {
                        Text(
                            stringResource(Res.string.resmulti_import_no_columns),
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        Text(
                            stringResource(
                                Res.string.resmulti_import_summary,
                                matrix.rows.size,
                                matrix.diffCellCount,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (matrix.diffCellCount > 0) {
                            Text(
                                stringResource(Res.string.compare_detail_diff_count, matrix.diffCellCount),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = AppSpacing.xs),
                            )
                        }
                        if (matrix.skippedHeaders.isNotEmpty()) {
                            Text(
                                stringResource(
                                    Res.string.resmulti_import_skipped_columns,
                                    matrix.skippedHeaders.joinToString(),
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(top = AppSpacing.xs),
                            )
                        }
                        Spacer(Modifier.height(AppSpacing.md))
                        StickyMultiCompareGrid(
                            matrix = matrix,
                            onDiffCellClick = { row, index, cell ->
                                selectedDiff = Triple(row, index, cell)
                            },
                            onCellLongPressCopy = ::copyCellWithSnackbar,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                onFailure = {
                    Text(
                        stringResource(Res.string.resmulti_import_parse_error, it.message.orEmpty()),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
            )
            Spacer(Modifier.height(AppSpacing.lg))
        }
    }
}
