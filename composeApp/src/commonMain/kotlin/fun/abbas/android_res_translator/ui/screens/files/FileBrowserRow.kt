package `fun`.abbas.android_res_translator.ui.screens.files

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.theme.AppCodeSmallTextStyle
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.files_directory_label
import org.jetbrains.compose.resources.stringResource
import `fun`.abbas.android_res_translator.ui.theme.AppCodeTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing

@Composable
fun FileBrowserFolderRow(
    folder: FileBrowserItem.Folder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = AppSpacing.md, horizontal = AppSpacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Folder,
                contentDescription = null,
                tint = colors.tertiary,
                modifier = Modifier.size(28.dp),
            )
            Column(modifier = Modifier.padding(start = AppSpacing.md)) {
                Text(folder.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(stringResource(Res.string.files_directory_label), style = AppCodeSmallTextStyle, color = colors.outline)
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
        )
    }
}

@Composable
fun FileBrowserXmlRow(
    file: FileBrowserItem.XmlFile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = AppSpacing.md, horizontal = AppSpacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
            Column(modifier = Modifier.padding(start = AppSpacing.md)) {
                Text(file.name, style = AppCodeTextStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${file.versionLabel} • ${file.sizeLabel}",
                    style = AppCodeSmallTextStyle,
                    color = colors.outline,
                )
            }
        }
        BoxWithConstraints {
            if (maxWidth >= 400.dp) {
                IconButton(onClick = { /* No download in first version */ }) {
                    Icon(Icons.Default.Download, contentDescription = "Download", tint = colors.onSurfaceVariant)
                }
            }
        }
    }
}
