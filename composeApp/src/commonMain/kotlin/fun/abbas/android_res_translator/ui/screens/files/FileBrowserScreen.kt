package `fun`.abbas.android_res_translator.ui.screens.files

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import `fun`.abbas.android_res_translator.ui.screens.main.TranslationProjectRepository
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing

@Composable
fun FileBrowserScreen(
    store: FileBrowserStore,
    recentProjects: TranslationProjectRepository,
    onOpenFile: (FileBrowserItem.XmlFile) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by store.state.collectAsState()
    val recentList by recentProjects.projects.collectAsState()

    LaunchedEffect(recentList) {
        store.refresh()
    }

    Column(
        modifier =
            modifier
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
    }
}
