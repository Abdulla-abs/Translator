package `fun`.abbas.android_res_translator.ui.screens.compare

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.core.resources.compare.CompareMatrix
import `fun`.abbas.android_res_translator.core.resources.compare.CompareMatrixBuilder
import `fun`.abbas.android_res_translator.core.resources.xml.StringsXmlCodec
import `fun`.abbas.android_res_translator.ui.components.grid.StickyCompareGrid
import `fun`.abbas.android_res_translator.ui.navigation.AppBackHandler
import `fun`.abbas.android_res_translator.ui.platform.rememberCopyToClipboardHandler
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.common_back
import androidrestranslator.composeapp.generated.resources.compare_cell_copied_snackbar
import androidrestranslator.composeapp.generated.resources.compare_detail_diff_count
import androidrestranslator.composeapp.generated.resources.compare_detail_parse_error
import androidrestranslator.composeapp.generated.resources.compare_detail_summary
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun CompareDetailScreen(
    project: CompareProject,
    repository: CompareProjectRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val leftXml = remember(project.id) { repository.readLeftXml(project) }
    val rightXml = remember(project.id) { repository.readRightXml(project) }
    val matrixResult =
        remember(project.id, leftXml, rightXml) {
            runCatching {
                val leftFile = StringsXmlCodec.parse(leftXml)
                val rightFile = StringsXmlCodec.parse(rightXml)
                CompareMatrixBuilder.build(
                    leftColumnLabel = project.leftLangLabel.ifBlank { langLabelFromFileName(project.leftDisplayName.orEmpty()) },
                    leftFile = leftFile,
                    rightColumnLabel = project.rightLangLabel.ifBlank { langLabelFromFileName(project.rightDisplayName.orEmpty()) },
                    rightFile = rightFile,
                )
            }
        }
    val copyToClipboard = rememberCopyToClipboardHandler()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back))
                }
                Column(modifier = Modifier.padding(start = AppSpacing.sm)) {
                    Text(
                        project.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.height(AppSpacing.md))
            matrixResult.fold(
                onSuccess = { matrix: CompareMatrix ->
                    val diffCount = matrix.rows.count { it.hasDifference }
                    Text(
                        stringResource(
                            Res.string.compare_detail_summary,
                            matrix.rows.size,
                            diffCount,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (diffCount > 0) {
                        Text(
                            stringResource(Res.string.compare_detail_diff_count, diffCount),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = AppSpacing.xs),
                        )
                    }
                    Spacer(Modifier.height(AppSpacing.md))
                    StickyCompareGrid(
                        matrix = matrix,
                        onCellLongPressCopy = ::copyCellWithSnackbar,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                onFailure = {
                    Text(
                        stringResource(Res.string.compare_detail_parse_error, it.message.orEmpty()),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
            )
            Spacer(Modifier.height(AppSpacing.lg))
        }
    }
}
