package `fun`.abbas.android_res_translator.ui.screens.compare

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.XmlFileAccess
import `fun`.abbas.android_res_translator.ui.components.UploadXmlCard
import `fun`.abbas.android_res_translator.ui.files.DroppedXmlFile
import `fun`.abbas.android_res_translator.ui.navigation.AppBackHandler
import `fun`.abbas.android_res_translator.ui.theme.AppControlShape
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.common_back
import androidrestranslator.composeapp.generated.resources.compare_run_button
import androidrestranslator.composeapp.generated.resources.compare_upload_left_hint
import androidrestranslator.composeapp.generated.resources.compare_upload_left_title
import androidrestranslator.composeapp.generated.resources.compare_upload_reupload_hint
import androidrestranslator.composeapp.generated.resources.compare_upload_right_hint
import androidrestranslator.composeapp.generated.resources.compare_upload_right_title
import androidrestranslator.composeapp.generated.resources.compare_upload_uploaded_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun CompareUploadScreen(
    project: CompareProject,
    repository: CompareProjectRepository,
    xmlFileAccess: XmlFileAccess,
    onBack: () -> Unit,
    onCompareReady: (CompareProject) -> Unit,
    modifier: Modifier = Modifier,
) {
    val projects by repository.projects.collectAsState()
    val current = projects.find { it.id == project.id } ?: project

    AppBackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(AppSpacing.gutter)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back))
                }
                Column(modifier = Modifier.padding(start = AppSpacing.sm)) {
                    Text(
                        current.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            BoxWithConstraints(Modifier.fillMaxWidth()) {
                if (maxWidth >= 700.dp) {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                        UploadSlot(
                            titleRes = Res.string.compare_upload_left_title,
                            hintRes = Res.string.compare_upload_left_hint,
                            uploadedName = current.leftDisplayName,
                            onPick = {
                                xmlFileAccess.launchPickXml { result ->
                                    result.onSuccess { xml ->
                                        repository.attachLeftFile(current.id, xml, "left.xml")
                                    }
                                }
                            },
                            onDrop = { files ->
                                files.firstOrNull()?.let { file ->
                                    repository.attachLeftFile(current.id, file.content, file.displayName)
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                        UploadSlot(
                            titleRes = Res.string.compare_upload_right_title,
                            hintRes = Res.string.compare_upload_right_hint,
                            uploadedName = current.rightDisplayName,
                            onPick = {
                                xmlFileAccess.launchPickXml { result ->
                                    result.onSuccess { xml ->
                                        repository.attachRightFile(current.id, xml, "right.xml")
                                    }
                                }
                            },
                            onDrop = { files ->
                                files.firstOrNull()?.let { file ->
                                    repository.attachRightFile(current.id, file.content, file.displayName)
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                        UploadSlot(
                            titleRes = Res.string.compare_upload_left_title,
                            hintRes = Res.string.compare_upload_left_hint,
                            uploadedName = current.leftDisplayName,
                            onPick = {
                                xmlFileAccess.launchPickXml { result ->
                                    result.onSuccess { xml ->
                                        repository.attachLeftFile(current.id, xml, "left.xml")
                                    }
                                }
                            },
                            onDrop = { files ->
                                files.firstOrNull()?.let { file ->
                                    repository.attachLeftFile(current.id, file.content, file.displayName)
                                }
                            },
                        )
                        UploadSlot(
                            titleRes = Res.string.compare_upload_right_title,
                            hintRes = Res.string.compare_upload_right_hint,
                            uploadedName = current.rightDisplayName,
                            onPick = {
                                xmlFileAccess.launchPickXml { result ->
                                    result.onSuccess { xml ->
                                        repository.attachRightFile(current.id, xml, "right.xml")
                                    }
                                }
                            },
                            onDrop = { files ->
                                files.firstOrNull()?.let { file ->
                                    repository.attachRightFile(current.id, file.content, file.displayName)
                                }
                            },
                        )
                    }
                }
            }

            Button(
                onClick = { onCompareReady(current) },
                enabled = current.isReadyToCompare,
                modifier = Modifier.fillMaxWidth(),
                shape = AppControlShape,
            ) {
                Text(stringResource(Res.string.compare_run_button))
            }
            Spacer(Modifier.height(AppSpacing.lg))
        }
    }
}

@Composable
private fun UploadSlot(
    titleRes: org.jetbrains.compose.resources.StringResource,
    hintRes: org.jetbrains.compose.resources.StringResource,
    uploadedName: String?,
    onPick: () -> Unit,
    onDrop: (List<DroppedXmlFile>) -> Unit,
    modifier: Modifier = Modifier,
) {
    UploadXmlCard(
        titleRes = titleRes,
        hintRes = hintRes,
        uploadedFileName = uploadedName,
        uploadedTitleRes = Res.string.compare_upload_uploaded_title,
        uploadedHintRes = Res.string.compare_upload_reupload_hint,
        onClick = onPick,
        onDrop = onDrop,
        modifier = modifier,
    )
}
