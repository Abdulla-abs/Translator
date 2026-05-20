package `fun`.abbas.android_res_translator.ui.screens.files

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import `fun`.abbas.android_res_translator.ui.components.AppGlassCard
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing

@Composable
fun FileListSection(
    items: List<FileBrowserItem>,
    onFolderClick: (FileBrowserItem.Folder) -> Unit,
    onFileClick: (FileBrowserItem.XmlFile) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    AppGlassCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("NAME", style = AppLabelCapsTextStyle, color = colors.onSurfaceVariant)
            Text("ACTION", style = AppLabelCapsTextStyle, color = colors.onSurfaceVariant)
        }
        HorizontalDivider(color = colors.outlineVariant)
        if (items.isEmpty()) {
            Text(
                "This directory is empty",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(vertical = AppSpacing.lg),
            )
        } else {
            Column {
                items.forEachIndexed { index, item ->
                    when (item) {
                        is FileBrowserItem.Folder ->
                            FileBrowserFolderRow(
                                folder = item,
                                onClick = { onFolderClick(item) },
                            )
                        is FileBrowserItem.XmlFile ->
                            FileBrowserXmlRow(
                                file = item,
                                onClick = { onFileClick(item) },
                            )
                    }
                    if (index < items.lastIndex) {
                        HorizontalDivider(color = colors.outlineVariant)
                    }
                }
            }
        }
    }
}
