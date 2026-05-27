package `fun`.abbas.android_res_translator.ui.screens.resmulti

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import `fun`.abbas.android_res_translator.ui.theme.AppTheme

@Preview
@Composable
private fun ResMultiProjectScreenLayoutPreview() {
    AppTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        ) {
            ResMultiProjectHeader(
                projectName = previewResMultiProject().displayName,
                onBackClick = {},
                showPushVersion = true,
                pushVersionEnabled = true,
                onPushVersionClick = {},
            )
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.gutter),
            ) {
                ResMultiFilesTab(
                    project = previewResMultiProject(),
                    versionIndex = previewResMultiVersionIndex(),
                    exportInProgress = false,
                    versionBusy = false,
                    onPickDirectory = {},
                    onExportAll = {},
                    onExportSingle = {},
                    onImportCompare = {},
                    onRestoreVersion = {},
                    onDeleteVersion = {},
                    modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.lg),
                )
            }
        }
    }
}
