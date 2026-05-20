package `fun`.abbas.android_res_translator.ui.files

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * Checks whether this app has all-files-access at runtime.
 */
fun hasAllFilesAccess(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        true // READ/WRITE permissions handled via manifest on pre-R
    }
}

/**
 * Checks whether legacy READ_EXTERNAL_STORAGE is granted (API 28-29).
 */
fun hasLegacyStoragePermission(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

/**
 * Returns true if any required storage permission is missing.
 */
fun needsStoragePermission(context: android.content.Context): Boolean {
    return !hasAllFilesAccess() || !hasLegacyStoragePermission(context)
}

/**
 * Composable that checks for and requests necessary file access permissions.
 * Renders the given [content] once all permissions are granted.
 *
 * On Android 11+, opens the system "All Files Access" settings screen.
 * On Android 9-10, requests READ/WRITE_EXTERNAL_STORAGE at runtime.
 */
@Composable
fun WithStoragePermission(
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    var permissionsGranted by remember {
        mutableStateOf(!needsStoragePermission(context))
    }
    var showRationale by remember { mutableStateOf(false) }
    var showManageDialog by remember { mutableStateOf(false) }

    // Launcher for legacy READ/WRITE permission (API <= 29)
    val legacyPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            permissionsGranted = true
        } else {
            showRationale = true
        }
    }

    // Launcher for MANAGE_EXTERNAL_STORAGE settings (API 30+)
    val manageStorageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Check again after returning from settings
        permissionsGranted = !needsStoragePermission(context)
        if (!permissionsGranted) {
            showRationale = true
        }
    }

    fun requestPermissions() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11+: Direct user to All Files Access settings
                showManageDialog = true
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                // Android 6-10: Runtime permission request
                legacyPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    )
                )
            }
            else -> {
                // Below Android 6: Permissions are granted at install time
                permissionsGranted = true
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            requestPermissions()
        }
    }

    // Dialog for All Files Access (Android 11+)
    if (showManageDialog) {
        AlertDialog(
            onDismissRequest = { showManageDialog = false },
            icon = {
                Icon(
                    Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("All Files Access Required") },
            text = {
                Text(
                    "KMP Translator needs permission to browse and manage files on your device's storage. " +
                        "Please grant \"All files access\" on the next screen.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = {
                    showManageDialog = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        manageStorageLauncher.launch(intent)
                    }
                }) {
                    Text("Grant Access")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showManageDialog = false
                    // Allow limited mode without permission
                    permissionsGranted = true
                }) {
                    Text("Skip")
                }
            }
        )
    }

    // Rationale dialog shown after denial
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Storage Permission Needed") },
            text = {
                Text(
                    "Without storage permission, you can only browse files within the app's private directory. " +
                        "Grant permission to browse your full device storage.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = {
                    showRationale = false
                    requestPermissions()
                }) {
                    Text("Try Again")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationale = false
                    permissionsGranted = true // Continue in limited mode
                }) {
                    Text("Continue Without")
                }
            }
        )
    }

    if (permissionsGranted) {
        content()
    } else if (!showManageDialog && !showRationale) {
        // Show a card while permission dialogs are being shown
        StoragePermissionBanner(onRequestPermission = { requestPermissions() })
    }
}

@Composable
private fun StoragePermissionBanner(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.FolderOpen,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(40.dp)
            )
            Text(
                "Storage Access Required",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                "Grant storage permission to browse and manage XML resource files on your device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onRequestPermission) {
                    Text("Grant Permission")
                }
            }
        }
    }
}
