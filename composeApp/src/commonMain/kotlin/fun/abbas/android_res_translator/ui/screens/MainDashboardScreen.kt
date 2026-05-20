package `fun`.abbas.android_res_translator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.TranslationServices
import `fun`.abbas.android_res_translator.ui.XmlFileAccess
import `fun`.abbas.android_res_translator.ui.screens.main.DashboardInsightSection
import `fun`.abbas.android_res_translator.ui.screens.main.FileProjectsSection
import `fun`.abbas.android_res_translator.ui.screens.main.InMemoryRecentXmlProjectRepository
import `fun`.abbas.android_res_translator.ui.screens.main.QuickTranslateSection
import `fun`.abbas.android_res_translator.ui.screens.main.RecentXmlProject
import `fun`.abbas.android_res_translator.ui.screens.main.recentProjectFromXml
import `fun`.abbas.android_res_translator.ui.settings.AppSettingsRepository
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing

@Composable
fun MainDashboardScreen(
    settings: AppSettingsRepository,
    services: TranslationServices,
    xmlFileAccess: XmlFileAccess,
    projectRepository: InMemoryRecentXmlProjectRepository,
    onNavigateToFiles: () -> Unit,
    onOpenProject: (RecentXmlProject) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snap by settings.snapshot.collectAsState()
    val scroll = rememberScrollState()
    var uploadCounter by remember { mutableIntStateOf(0) }

    fun onUploadXml(xml: String) {
        uploadCounter += 1
        val name = if (uploadCounter == 1) "strings.xml" else "strings_$uploadCounter.xml"
        projectRepository.addOrUpdate(recentProjectFromXml(xml, name))
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val showFab = maxWidth < 840.dp

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier =
                    Modifier
                        .widthIn(max = 1280.dp)
                        .fillMaxSize()
                        .verticalScroll(scroll)
                        .padding(AppSpacing.gutter),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
            ) {
                QuickTranslateSection(
                    settings = settings,
                    services = services,
                    defaultFrom = snap.defaultSourceLang,
                    defaultTo = snap.defaultTargetLang,
                )
                FileProjectsSection(
                    repository = projectRepository,
                    onViewAllClick = onNavigateToFiles,
                    onUploadClick = {
                        xmlFileAccess.launchPickXml { result ->
                            result.onSuccess(::onUploadXml)
                        }
                    },
                    onProjectClick = onOpenProject,
                )
                DashboardInsightSection()
                Spacer(Modifier.height(if (showFab) 88.dp else AppSpacing.lg))
            }

            if (showFab) {
                FloatingActionButton(
                    onClick = {
                        xmlFileAccess.launchPickXml { result ->
                            result.onSuccess(::onUploadXml)
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(AppSpacing.margin),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Upload XML")
                }
            }
        }
    }
}
