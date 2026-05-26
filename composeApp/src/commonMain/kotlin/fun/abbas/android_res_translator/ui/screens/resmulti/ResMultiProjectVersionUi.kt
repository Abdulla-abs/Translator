package `fun`.abbas.android_res_translator.ui.screens.resmulti

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.persistence.ResMultiVersionIndex
import `fun`.abbas.android_res_translator.persistence.ResMultiVersionIndexEntry
import `fun`.abbas.android_res_translator.persistence.ResProjectVersionStore
import `fun`.abbas.android_res_translator.ui.components.AppSectionCard
import `fun`.abbas.android_res_translator.ui.components.AppSectionTitle
import `fun`.abbas.android_res_translator.ui.theme.AppControlShape
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.common_cancel
import androidrestranslator.composeapp.generated.resources.common_confirm
import androidrestranslator.composeapp.generated.resources.resmulti_detail_version_count
import androidrestranslator.composeapp.generated.resources.resmulti_detail_version_push
import androidrestranslator.composeapp.generated.resources.resmulti_detail_versions_title
import androidrestranslator.composeapp.generated.resources.resmulti_detail_version_list
import androidrestranslator.composeapp.generated.resources.resmulti_version_actions
import androidrestranslator.composeapp.generated.resources.resmulti_version_delete
import androidrestranslator.composeapp.generated.resources.resmulti_version_delete_cascade_warning
import androidrestranslator.composeapp.generated.resources.resmulti_version_delete_confirm_countdown
import androidrestranslator.composeapp.generated.resources.resmulti_version_delete_confirm_message
import androidrestranslator.composeapp.generated.resources.resmulti_version_delete_confirm_title
import androidrestranslator.composeapp.generated.resources.resmulti_version_dirty_hint
import androidrestranslator.composeapp.generated.resources.resmulti_version_head_badge
import androidrestranslator.composeapp.generated.resources.resmulti_version_list_dialog_title
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
    onPushVersion: (String) -> Unit,
    onRestoreVersion: (String) -> Unit,
    onDeleteVersion: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPushDialog by remember { mutableStateOf(false) }
    var showListDialog by remember { mutableStateOf(false) }
    val canPush = project.dirty || versionIndex.dirty

    AppSectionCard(modifier = modifier) {
        AppSectionTitle(stringResource(Res.string.resmulti_detail_versions_title))
        Text(
            stringResource(Res.string.resmulti_detail_version_count, versionIndex.versions.size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = AppSpacing.xs),
        )
        if (canPush) {
            Text(
                stringResource(Res.string.resmulti_version_dirty_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = AppSpacing.sm),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            ResMultiVersionActionButton(
                label = stringResource(Res.string.resmulti_detail_version_push),
                icon = Icons.Default.Publish,
                enabled = canPush,
                onClick = { showPushDialog = true },
            )
            ResMultiVersionActionButton(
                label = stringResource(Res.string.resmulti_detail_version_list),
                icon = Icons.Default.History,
                enabled = versionIndex.versions.isNotEmpty(),
                onClick = { showListDialog = true },
            )
        }
    }

    if (showPushDialog) {
        PushVersionDialog(
            onDismiss = { showPushDialog = false },
            onConfirm = { name ->
                showPushDialog = false
                onPushVersion(name)
            },
        )
    }

    if (showListDialog) {
        VersionListDialog(
            versionIndex = versionIndex,
            onDismiss = { showListDialog = false },
            onRestore = { versionId ->
                showListDialog = false
                onRestoreVersion(versionId)
            },
            onDelete = { versionId ->
                showListDialog = false
                onDeleteVersion(versionId)
            },
        )
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
    val needsCountdown = ResProjectVersionStore.requiresCascadeDeleteCountdown(versionIndex, version.id)
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
                        stringResource(Res.string.resmulti_version_delete_confirm_countdown, countdown)
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
private fun PushVersionDialog(
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
private fun VersionListDialog(
    versionIndex: ResMultiVersionIndex,
    onDismiss: () -> Unit,
    onRestore: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.resmulti_version_list_dialog_title)) },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                versionIndex.versions.forEach { version ->
                    VersionListRow(
                        version = version,
                        isHead = version.id == versionIndex.headId,
                        canDelete = versionIndex.versions.size > 1,
                        onRestore = { onRestore(version.id) },
                        onDelete = { onDelete(version.id) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.common_confirm))
            }
        },
    )
}

@Composable
private fun VersionListRow(
    version: ResMultiVersionIndexEntry,
    isHead: Boolean,
    canDelete: Boolean,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        version.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (isHead) {
                        Text(
                            " · ${stringResource(Res.string.resmulti_version_head_badge)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
                Text(
                    version.id,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    formatEpochAgo(version.createdAtEpochMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(Res.string.resmulti_version_actions))
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
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Text(label, modifier = Modifier.padding(start = AppSpacing.sm))
    }
}
