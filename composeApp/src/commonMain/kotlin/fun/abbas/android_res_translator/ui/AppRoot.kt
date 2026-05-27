package `fun`.abbas.android_res_translator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import `fun`.abbas.android_res_translator.ui.navigation.RootRoute
import `fun`.abbas.android_res_translator.ui.navigation.rootNavSavedStateConfig
import `fun`.abbas.android_res_translator.ui.screens.AboutScreen
import `fun`.abbas.android_res_translator.ui.screens.FilesScreen
import `fun`.abbas.android_res_translator.ui.screens.MainDashboardScreen
import `fun`.abbas.android_res_translator.ui.screens.SettingsScreen
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorControllerStore
import `fun`.abbas.android_res_translator.ui.screens.main.createTranslationProjectRepository
import `fun`.abbas.android_res_translator.ui.screens.compare.createCompareProjectRepository
import `fun`.abbas.android_res_translator.ui.screens.resmulti.createResMultiProjectRepository
import `fun`.abbas.android_res_translator.ui.settings.AppSettingsRepository
import `fun`.abbas.android_res_translator.ui.settings.RepositorySecretsProvider
import `fun`.abbas.android_res_translator.ui.settings.createAppSettingsRepository
import `fun`.abbas.android_res_translator.ui.theme.AppControlShape
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import `fun`.abbas.android_res_translator.ui.i18n.ProvideAppLocale
import `fun`.abbas.android_res_translator.ui.theme.AppTheme
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.app_name
import androidrestranslator.composeapp.generated.resources.app_name_sidebar
import androidrestranslator.composeapp.generated.resources.common_settings
import androidrestranslator.composeapp.generated.resources.files_search_placeholder
import androidrestranslator.composeapp.generated.resources.nav_about
import androidrestranslator.composeapp.generated.resources.nav_files
import androidrestranslator.composeapp.generated.resources.nav_settings
import androidrestranslator.composeapp.generated.resources.nav_translate
import org.jetbrains.compose.resources.stringResource

private data class RootTabItem(
    val route: RootRoute,
    val labelRes: org.jetbrains.compose.resources.StringResource,
    val icon: ImageVector,
)

@Composable
private fun rememberRootTabs(): List<RootTabItem> =
    listOf(
        RootTabItem(RootRoute.Translate, Res.string.nav_translate, Icons.Default.Translate),
        RootTabItem(RootRoute.Files, Res.string.nav_files, Icons.Default.FolderOpen),
        RootTabItem(RootRoute.Settings, Res.string.nav_settings, Icons.Default.Settings),
        RootTabItem(RootRoute.About, Res.string.nav_about, Icons.Default.Info),
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(settingsRepository: AppSettingsRepository = createAppSettingsRepository()) {
    val secrets = remember(settingsRepository) { RepositorySecretsProvider(settingsRepository.snapshot) }
    val services = remember(secrets) { TranslationServices(secrets) }
    DisposableEffect(services) {
        onDispose { services.close() }
    }
    val xmlFileAccess = rememberXmlFileAccess()
    val editorScope = rememberCoroutineScope()
    val projectRepository = remember(editorScope) { createTranslationProjectRepository(editorScope) }
    val compareProjectRepository = remember(editorScope) { createCompareProjectRepository(editorScope) }
    val resMultiProjectRepository = remember(editorScope) { createResMultiProjectRepository(editorScope) }
    val editorControllerStore =
        remember(services, editorScope) {
            FileEditorControllerStore(services, editorScope)
        }
    val backStack = rememberNavBackStack(rootNavSavedStateConfig, RootRoute.Translate)
    var filesSearchQuery by remember { mutableStateOf("") }
    val currentRoute = backStack.lastOrNull()

    // 使用 StateFlow 专用 collectAsState（勿传 Flow 的 initial = 空快照，否则首帧与部分平台下订阅行为异常）
    val snap by settingsRepository.snapshot.collectAsState()
    val rootTabs = rememberRootTabs()
    ProvideAppLocale(locale = snap.uiLocale) {
        AppTheme(appearance = snap.appAppearance) {
        val colors = MaterialTheme.colorScheme
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isDesktop = maxWidth >= 768.dp
            if (isDesktop) {
                // Desktop Layout
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Sidebar
                    Column(
                        modifier = Modifier
                            .width(256.dp)
                            .fillMaxHeight()
                            .background(colors.surfaceContainerLow)
                            .drawBehind {
                                drawLine(
                                    color = colors.outlineVariant.copy(alpha = 0.3f),
                                    start = Offset(size.width, 0f),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Terminal,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(24.dp),
                            )
                            Text(
                                stringResource(Res.string.app_name_sidebar),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = colors.primary,
                            )
                        }
                        
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            rootTabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                Surface(
                                    onClick = { navigateToRootTab(backStack, tab.route) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) colors.secondaryContainer else Color.Transparent,
                                    contentColor = if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Icon(
                                            tab.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Text(
                                            stringResource(tab.labelRes).uppercase(),
                                            style = AppLabelCapsTextStyle,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Right Content Scaffold (no bottom bar)
                    val showFilesSearch = currentRoute == RootRoute.Files
                    Scaffold(
                        containerColor = colors.background,
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineSmall)
                                },
                                actions = {
                                    if (showFilesSearch) {
                                        OutlinedTextField(
                                            value = filesSearchQuery,
                                            onValueChange = { filesSearchQuery = it },
                                            placeholder = { Text(stringResource(Res.string.files_search_placeholder)) },
                                            leadingIcon = {
                                                Icon(Icons.Default.Search, contentDescription = null)
                                            },
                                            singleLine = true,
                                            shape = AppControlShape,
                                            modifier = Modifier.widthIn(max = 220.dp).padding(end = 8.dp),
                                        )
                                    }
                                    IconButton(onClick = { navigateToRootTab(backStack, RootRoute.Settings) }) {
                                        Icon(Icons.Default.Settings, contentDescription = stringResource(Res.string.common_settings))
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = colors.background,
                                    titleContentColor = colors.primary,
                                ),
                            )
                        }
                    ) { innerPadding ->
                        NavDisplay(
                            backStack = backStack,
                            onBack = { navigateBackFromRoot(backStack) },
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
                            entryProvider = entryProvider {
                                entry<RootRoute.Translate> {
                                    MainDashboardScreen(
                                        settings = settingsRepository,
                                        services = services,
                                        xmlFileAccess = xmlFileAccess,
                                        projectRepository = projectRepository,
                                        compareProjectRepository = compareProjectRepository,
                                        resMultiProjectRepository = resMultiProjectRepository,
                                        editorControllerStore = editorControllerStore,
                                        onNavigateToFiles = { navigateToRootTab(backStack, RootRoute.Files) },
                                    )
                                }
                                entry<RootRoute.Files> {
                                    FilesScreen(
                                        settings = settingsRepository,
                                        services = services,
                                        xmlFileAccess = xmlFileAccess,
                                        projectRepository = projectRepository,
                                        editorControllerStore = editorControllerStore,
                                        searchQuery = filesSearchQuery,
                                        onSearchQueryChange = { filesSearchQuery = it },
                                    )
                                }
                                entry<RootRoute.Settings> {
                                    SettingsScreen(repository = settingsRepository)
                                }
                                entry<RootRoute.About> {
                                    AboutScreen()
                                }
                            },
                        )
                    }
                }
            } else {
                // Mobile Layout (Scaffold with bottom navigation)
                val showFilesSearch = currentRoute == RootRoute.Files && maxWidth >= 600.dp
                Scaffold(
                    containerColor = colors.background,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineSmall)
                            },
                            navigationIcon = {
                                Icon(
                                    Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.padding(start = 12.dp),
                                )
                            },
                            actions = {
                                if (showFilesSearch) {
                                    OutlinedTextField(
                                        value = filesSearchQuery,
                                        onValueChange = { filesSearchQuery = it },
                                        placeholder = { Text(stringResource(Res.string.files_search_placeholder)) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Search, contentDescription = null)
                                        },
                                        singleLine = true,
                                        shape = AppControlShape,
                                        modifier = Modifier.widthIn(max = 220.dp).padding(end = 8.dp),
                                    )
                                }
                                IconButton(onClick = { navigateToRootTab(backStack, RootRoute.Settings) }) {
                                    Icon(Icons.Default.Settings, contentDescription = stringResource(Res.string.common_settings))
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = colors.background,
                                titleContentColor = colors.primary,
                            ),
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = colors.surfaceContainerLowest,
                            tonalElevation = 0.dp,
                        ) {
                            val current = backStack.lastOrNull()
                            rootTabs.forEach { tab ->
                                NavigationBarItem(
                                    selected = current == tab.route,
                                    onClick = { navigateToRootTab(backStack, tab.route) },
                                    icon = {
                                        Icon(
                                            tab.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    label = { Text(stringResource(tab.labelRes)) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = colors.onPrimaryContainer,
                                        selectedTextColor = colors.onSurface,
                                        indicatorColor = colors.primaryContainer,
                                        unselectedIconColor = colors.onSurfaceVariant,
                                        unselectedTextColor = colors.onSurfaceVariant,
                                    ),
                                )
                            }
                        }
                    },
                ) { innerPadding ->
                    NavDisplay(
                        backStack = backStack,
                        onBack = { navigateBackFromRoot(backStack) },
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        entryProvider = entryProvider {
                            entry<RootRoute.Translate> {
                                MainDashboardScreen(
                                    settings = settingsRepository,
                                    services = services,
                                    xmlFileAccess = xmlFileAccess,
                                    projectRepository = projectRepository,
                                    compareProjectRepository = compareProjectRepository,
                                    resMultiProjectRepository = resMultiProjectRepository,
                                    editorControllerStore = editorControllerStore,
                                    onNavigateToFiles = { navigateToRootTab(backStack, RootRoute.Files) },
                                )
                            }
                            entry<RootRoute.Files> {
                                FilesScreen(
                                    settings = settingsRepository,
                                    services = services,
                                    xmlFileAccess = xmlFileAccess,
                                    projectRepository = projectRepository,
                                    editorControllerStore = editorControllerStore,
                                    searchQuery = filesSearchQuery,
                                    onSearchQueryChange = { filesSearchQuery = it },
                                )
                            }
                            entry<RootRoute.Settings> {
                                SettingsScreen(repository = settingsRepository)
                            }
                            entry<RootRoute.About> {
                                AboutScreen()
                            }
                        },
                    )
                }
            }
        }
        }
    }
}

private fun navigateToRootTab(
    backStack: MutableList<NavKey>,
    route: RootRoute,
) {
    if (backStack.lastOrNull() == route) return
    backStack.clear()
    backStack.add(route)
}

private fun navigateBackFromRoot(backStack: MutableList<NavKey>) {
    when (backStack.lastOrNull()) {
        RootRoute.Translate, null -> backStack.removeLastOrNull()
        else -> navigateToRootTab(backStack, RootRoute.Translate)
    }
}
