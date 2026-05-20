package `fun`.abbas.android_res_translator.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberXmlFileAccess(): XmlFileAccess {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pickCb by remember { mutableStateOf<((Result<String>) -> Unit)?>(null) }
    var savePayload by remember { mutableStateOf<Pair<String, String>?>(null) }
    var saveCb by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }

    val pickLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            val cb = pickCb
            pickCb = null
            if (cb == null) return@rememberLauncherForActivityResult
            if (uri == null) {
                cb(Result.failure(Exception("Cancelled")))
                return@rememberLauncherForActivityResult
            }
            scope.launch(Dispatchers.IO) {
                val result =
                    try {
                        val text =
                            context.contentResolver.openInputStream(uri)!!.use { stream ->
                                stream.readBytes().decodeToString()
                            }
                        Result.success(text)
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                withContext(Dispatchers.Main) { cb(result) }
            }
        }

    val saveLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/xml")) { uri: Uri? ->
            val cb = saveCb
            val payload = savePayload
            saveCb = null
            savePayload = null
            if (cb == null || payload == null) return@rememberLauncherForActivityResult
            if (uri == null) {
                cb(false)
                return@rememberLauncherForActivityResult
            }
            scope.launch(Dispatchers.IO) {
                val ok =
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(payload.first.encodeToByteArray())
                        }
                        true
                    } catch (_: Exception) {
                        false
                    }
                withContext(Dispatchers.Main) { cb(ok) }
            }
        }

    return remember {
        object : XmlFileAccess {
            override fun launchPickXml(onResult: (Result<String>) -> Unit) {
                pickCb = onResult
                pickLauncher.launch(arrayOf("text/xml", "application/xml", "*/*"))
            }

            override fun launchSaveXml(
                content: String,
                suggestedName: String,
                onDone: (Boolean) -> Unit,
            ) {
                savePayload = content to suggestedName
                saveCb = onDone
                saveLauncher.launch(suggestedName)
            }
        }
    }
}

@Composable
actual fun rememberDirectoryPicker(onResult: (String?) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        onResult(uri?.path ?: uri?.toString())
    }
    return remember {
        {
            launcher.launch(null)
        }
    }
}
