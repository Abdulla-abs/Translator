package `fun`.abbas.android_res_translator.ui.screens.resmulti

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.theme.AppTheme
import `fun`.abbas.android_res_translator.persistence.ResMultiVersionIndex
import `fun`.abbas.android_res_translator.persistence.ResMultiVersionIndexEntry
import `fun`.abbas.android_res_translator.persistence.ResProjectVersionStore
import `fun`.abbas.android_res_translator.persistence.versionsNewestFirst
import `fun`.abbas.android_res_translator.ui.theme.AppCodeSmallTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppControlShape
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.common_cancel
import androidrestranslator.composeapp.generated.resources.common_confirm
import androidrestranslator.composeapp.generated.resources.resmulti_detail_version_count
import androidrestranslator.composeapp.generated.resources.resmulti_detail_versions_title
import androidrestranslator.composeapp.generated.resources.resmulti_version_actions
import androidrestranslator.composeapp.generated.resources.resmulti_version_delete
import androidrestranslator.composeapp.generated.resources.resmulti_version_delete_cascade_warning
import androidrestranslator.composeapp.generated.resources.resmulti_version_delete_confirm_countdown
import androidrestranslator.composeapp.generated.resources.resmulti_version_delete_confirm_message
import androidrestranslator.composeapp.generated.resources.resmulti_version_delete_confirm_title
import androidrestranslator.composeapp.generated.resources.resmulti_version_dirty_hint
import androidrestranslator.composeapp.generated.resources.resmulti_version_head_badge
import androidrestranslator.composeapp.generated.resources.resmulti_version_push_dialog_title
import androidrestranslator.composeapp.generated.resources.resmulti_version_push_name_label
import androidrestranslator.composeapp.generated.resources.resmulti_version_restore
import androidrestranslator.composeapp.generated.resources.resmulti_version_restore_confirm_message
import androidrestranslator.composeapp.generated.resources.resmulti_version_restore_confirm_title
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun ResMultiProjectVersionSection(
    project: ResMultiProject,
    versionIndex: ResMultiVersionIndex,
    onRestoreVersion: (String) -> Unit,
    onDeleteVersion: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val canPush = project.dirty || versionIndex.dirty

    ResMultiSectionCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(Res.string.resmulti_detail_versions_title).uppercase(),
                style = AppLabelCapsTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(
                    Res.string.resmulti_detail_version_count,
                    versionIndex.versions.size
                ),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = AppSpacing.xs),
            )
        }

        if (canPush) {
            Text(
                stringResource(Res.string.resmulti_version_dirty_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = AppSpacing.sm),
            )
        }

        if (versionIndex.versions.isNotEmpty()) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = AppSpacing.md),
            )
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                versionIndex.versionsNewestFirst.forEach { version ->
                    ResMultiVersionInlineRow(
                        version = version,
                        isHead = version.id == versionIndex.headId,
                        canDelete = versionIndex.versions.size > 1,
                        onRestore = { onRestoreVersion(version.id) },
                        onDelete = { onDeleteVersion(version.id) },
                    )
                }
            }
        }
    }
}

@Composable
fun RestoreVersionConfirmDialog(
    version: ResMultiVersionIndexEntry,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.resmulti_version_restore_confirm_title)) },
        text = {
            Text(
                stringResource(
                    Res.string.resmulti_version_restore_confirm_message,
                    version.displayName,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.common_cancel))
            }
        },
    )
}

@Composable
fun DeleteVersionConfirmDialog(
    version: ResMultiVersionIndexEntry,
    versionIndex: ResMultiVersionIndex,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val needsCountdown =
        ResProjectVersionStore.requiresCascadeDeleteCountdown(versionIndex, version.id)
    var countdown by remember(version.id) { mutableIntStateOf(if (needsCountdown) ResProjectVersionStore.cascadeDeleteCountdownSeconds else 0) }

    LaunchedEffect(needsCountdown, version.id) {
        if (!needsCountdown) return@LaunchedEffect
        countdown = ResProjectVersionStore.cascadeDeleteCountdownSeconds
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.resmulti_version_delete_confirm_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Text(
                    stringResource(
                        Res.string.resmulti_version_delete_confirm_message,
                        version.displayName,
                    ),
                )
                if (needsCountdown) {
                    Text(
                        stringResource(Res.string.resmulti_version_delete_cascade_warning),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !needsCountdown || countdown == 0,
            ) {
                Text(
                    if (needsCountdown && countdown > 0) {
                        stringResource(
                            Res.string.resmulti_version_delete_confirm_countdown,
                            countdown
                        )
                    } else {
                        stringResource(Res.string.common_confirm)
                    },
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.common_cancel))
            }
        },
    )
}

@Composable
internal fun PushVersionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.resmulti_version_push_dialog_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Res.string.resmulti_version_push_name_label)) },
                singleLine = true,
                shape = AppControlShape,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim()) },
                enabled = name.trim().isNotEmpty(),
            ) {
                Text(stringResource(Res.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.common_cancel))
            }
        },
    )
}

@Composable
private fun ResMultiVersionInlineRow(
    version: ResMultiVersionIndexEntry,
    isHead: Boolean,
    canDelete: Boolean,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            modifier = Modifier.weight(1f),
        ) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically // 竖直居中
                ) {
                    Text(
                        version.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    if (isHead) {
                        Text(
                            stringResource(Res.string.resmulti_version_head_badge),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    ),
                ) {
                    Text(
                        version.id,
                        style = AppCodeSmallTextStyle.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                formatEpochAgo(version.createdAtEpochMs),
                style = AppCodeSmallTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(Res.string.resmulti_version_actions)
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.resmulti_version_restore)) },
                        onClick = {
                            menuExpanded = false
                            onRestore()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.resmulti_version_delete)) },
                        enabled = canDelete,
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun ResMultiVersionActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = AppControlShape,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            colors.outlineVariant.copy(alpha = 0.5f)
        ),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Text(label, modifier = Modifier.padding(start = AppSpacing.sm))
    }
}

@Preview
@Composable
private fun ResMultiProjectVersionSectionPreview() {
    AppTheme {
        ResMultiProjectVersionSection(
            project = previewResMultiProject(),
            versionIndex = previewResMultiVersionIndex(),
            onRestoreVersion = {},
            onDeleteVersion = {},
            modifier = Modifier.padding(AppSpacing.md),
        )
    }
}

@Preview
@Composable
private fun ResMultiProjectVersionSectionDirtyPreview() {
    AppTheme {
        ResMultiProjectVersionSection(
            project = previewResMultiProject(dirty = true),
            versionIndex = previewResMultiVersionIndex(dirty = true),
            onRestoreVersion = {},
            onDeleteVersion = {},
            modifier = Modifier.padding(AppSpacing.md),
        )
    }
}

@Preview
@Composable
private fun PushVersionDialogPreview() {
    AppTheme {
        PushVersionDialog(onDismiss = {}, onConfirm = {})
    }
}

@Preview
@Composable
private fun RestoreVersionConfirmDialogPreview() {
    AppTheme {
        RestoreVersionConfirmDialog(
            version = previewResMultiVersionEntry(),
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Preview
@Composable
private fun DeleteVersionConfirmDialogPreview() {
    val index = previewResMultiVersionIndex()
    AppTheme {
        DeleteVersionConfirmDialog(
            version = index.versions.first(),
            versionIndex = index,
            onDismiss = {},
            onConfirm = {},
        )
    }
}
