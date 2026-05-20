package `fun`.abbas.android_res_translator.ui.screens.files

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.XmlFileAccess
import `fun`.abbas.android_res_translator.ui.screens.main.InMemoryRecentXmlProjectRepository
import `fun`.abbas.android_res_translator.ui.screens.main.recentProjectFromXml
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import kotlin.random.Random

@Composable
fun FileBrowserScreen(
    store: FileBrowserStore,
    recentProjects: InMemoryRecentXmlProjectRepository,
    xmlFileAccess: XmlFileAccess,
    onOpenFile: (FileBrowserItem.XmlFile) -> Unit,
    showCompactSearch: Boolean,
    compactSearchQuery: String,
    onCompactSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by store.state.collectAsState()
    val recentList by recentProjects.projects.collectAsState()

    LaunchedEffect(recentList) {
        store.refresh()
    }

    LaunchedEffect(compactSearchQuery, showCompactSearch) {
        if (showCompactSearch) {
            store.setSearchQuery(compactSearchQuery)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(AppSpacing.gutter),
        ) {
            FileBreadcrumbBar(
                pathSegments = state.pathSegments,
                onHomeClick = store::navigateHome,
                onSegmentClick = store::navigateToSegment,
                onFilterClick = { /* Equivalent to search in first version */ },
            )
            Spacer(Modifier.height(AppSpacing.lg))
            FileListSection(
                items = state.items,
                onFolderClick = store::enterFolder,
                onFileClick = onOpenFile,
            )
            Spacer(Modifier.height(88.dp))
        }
        FloatingActionButton(
            onClick = {
                xmlFileAccess.launchPickXml { result ->
                    result.onSuccess { xml ->
                        val name = "strings_${Random.nextInt(100_000)}.xml"
                        recentProjects.addOrUpdate(recentProjectFromXml(xml, name))
                        if (state.pathSegments != listOf("src", "commonMain", "resources")) {
                            store.navigateToResources()
                        } else {
                            store.refresh()
                        }
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(AppSpacing.margin),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Icon(Icons.Default.UploadFile, contentDescription = "Upload File")
        }
    }
}

