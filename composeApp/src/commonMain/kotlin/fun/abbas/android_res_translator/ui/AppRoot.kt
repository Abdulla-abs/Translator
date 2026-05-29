package `fun`.abbas.android_res_translator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PermanentNavigationDrawer
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import `fun`.abbas.android_res_translator.ui.i18n.ProvideAppLocale
import `fun`.abbas.android_res_translator.ui.navigation.RootRoute
import `fun`.abbas.android_res_translator.ui.navigation.rootNavSavedStateConfig
import `fun`.abbas.android_res_translator.ui.screens.AboutScreen
import `fun`.abbas.android_res_translator.ui.screens.FilesScreen
import `fun`.abbas.android_res_translator.ui.screens.MainDashboardScreen
import `fun`.abbas.android_res_translator.ui.screens.SettingsScreen
import `fun`.abbas.android_res_translator.ui.screens.compare.CompareProjectRepository
import `fun`.abbas.android_res_translator.ui.screens.compare.createCompareProjectRepository
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorControllerStore
import `fun`.abbas.android_res_translator.ui.screens.main.TranslationProjectRepository
import `fun`.abbas.android_res_translator.ui.screens.main.createTranslationProjectRepository
import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiProjectRepository
import `fun`.abbas.android_res_translator.ui.screens.resmulti.createResMultiProjectRepository
import `fun`.abbas.android_res_translator.ui.settings.AppSettingsRepository
import `fun`.abbas.android_res_translator.ui.settings.RepositorySecretsProvider
import `fun`.abbas.android_res_translator.ui.settings.createAppSettingsRepository
import `fun`.abbas.android_res_translator.ui.theme.AppControlShape
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppSidebarShape
import `fun`.abbas.android_res_translator.ui.theme.AppTheme
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.app_name
import androidrestranslator.composeapp.generated.resources.app_name_sidebar
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

    val snap by settingsRepository.snapshot.collectAsState()
    val rootTabs = rememberRootTabs()
    ProvideAppLocale(locale = snap.uiLocale) {
        AppTheme(appearance = snap.appAppearance) {
            val colors = MaterialTheme.colorScheme
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val navLayout = appNavigationLayout(maxWidth, maxHeight)
                val navDeps =
                    AppRootNavDependencies(
                        backStack = backStack,
                        settingsRepository = settingsRepository,
                        services = services,
                        xmlFileAccess = xmlFileAccess,
                        projectRepository = projectRepository,
                        compareProjectRepository = compareProjectRepository,
                        resMultiProjectRepository = resMultiProjectRepository,
                        editorControllerStore = editorControllerStore,
                        filesSearchQuery = filesSearchQuery,
                        onFilesSearchQueryChange = { filesSearchQuery = it },
                    )

                when (navLayout) {
                    AppNavigationLayout.Expanded -> {
                        PermanentNavigationDrawer(
                            drawerContent = {
                                AppRootDrawerSheet(
                                    rootTabs = rootTabs,
                                    currentRoute = currentRoute,
                                    backStack = backStack,
                                )
                            },
                        ) {
                            AppRootMainScaffold(
                                containerColor = colors.background,
                                currentRoute = currentRoute,
                                rootTabs = rootTabs,
                                backStack = backStack,
                                showBottomNav = false,
                                filesSearchQuery = filesSearchQuery,
                                onFilesSearchQueryChange = { filesSearchQuery = it },
                                navDeps = navDeps,
                            )
                        }
                    }
                    AppNavigationLayout.Medium -> {
                        Row(Modifier.fillMaxSize()) {
                            AppRootNavigationRail(
                                rootTabs = rootTabs,
                                currentRoute = currentRoute,
                                onTabClick = { route -> navigateToRootTab(backStack, route) },
                            )
                            Box(Modifier.weight(1f).fillMaxHeight()) {
                                AppRootMainScaffold(
                                    containerColor = colors.background,
                                    currentRoute = currentRoute,
                                    rootTabs = rootTabs,
                                    backStack = backStack,
                                    showBottomNav = false,
                                    filesSearchQuery = filesSearchQuery,
                                    onFilesSearchQueryChange = { filesSearchQuery = it },
                                    navDeps = navDeps,
                                )
                            }
                        }
                    }
                    AppNavigationLayout.Compact -> {
                        AppRootMainScaffold(
                            containerColor = colors.background,
                            currentRoute = currentRoute,
                            rootTabs = rootTabs,
                            backStack = backStack,
                            showBottomNav = true,
                            filesSearchQuery = filesSearchQuery,
                            onFilesSearchQueryChange = { filesSearchQuery = it },
                            navDeps = navDeps,
                        )
                    }
                }
            }
        }
    }
}

private data class AppRootNavDependencies(
    val backStack: MutableList<NavKey>,
    val settingsRepository: AppSettingsRepository,
    val services: TranslationServices,
    val xmlFileAccess: XmlFileAccess,
    val projectRepository: TranslationProjectRepository,
    val compareProjectRepository: CompareProjectRepository,
    val resMultiProjectRepository: ResMultiProjectRepository,
    val editorControllerStore: FileEditorControllerStore,
    val filesSearchQuery: String,
    val onFilesSearchQueryChange: (String) -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRootDrawerSheet(
    rootTabs: List<RootTabItem>,
    currentRoute: NavKey?,
    backStack: MutableList<NavKey>,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.width(256.dp).fillMaxHeight(),
        shape = AppSidebarShape,
        color = colors.surfaceContainerLow,
    ) {
        AppRootDrawerContent(
            rootTabs = rootTabs,
            currentRoute = currentRoute,
            onTabClick = { route -> navigateToRootTab(backStack, route) },
        )
    }
}

@Composable
private fun AppRootDrawerContent(
    rootTabs: List<RootTabItem>,
    currentRoute: NavKey?,
    onTabClick: (RootRoute) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
        Spacer(Modifier.height(8.dp))
        rootTabs.forEach { tab ->
            NavigationDrawerItem(
                icon = {
                    Icon(
                        tab.icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
                label = {
                    Text(
                        stringResource(tab.labelRes).uppercase(),
                        style = AppLabelCapsTextStyle,
                    )
                },
                selected = currentRoute == tab.route,
                onClick = { onTabClick(tab.route) },
                shape = AppControlShape,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRootMainScaffold(
    containerColor: androidx.compose.ui.graphics.Color,
    currentRoute: NavKey?,
    rootTabs: List<RootTabItem>,
    backStack: MutableList<NavKey>,
    showBottomNav: Boolean,
    filesSearchQuery: String,
    onFilesSearchQueryChange: (String) -> Unit,
    navDeps: AppRootNavDependencies,
) {
    val colors = MaterialTheme.colorScheme
    val showFilesSearch = currentRoute == RootRoute.Files
    val hideTopBarTitle = showBottomNav && showFilesSearch
    Scaffold(
        containerColor = containerColor,
        topBar = {
            TopAppBar(
                title = {
                    if (!hideTopBarTitle) {
                        Text(
                            stringResource(Res.string.app_name),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                },
                navigationIcon = {
                    if (showBottomNav) {
                        Icon(
                            Icons.Default.Terminal,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                },
                actions = {
                    if (showFilesSearch) {
                        AppRootFilesSearchField(
                            query = filesSearchQuery,
                            onQueryChange = onFilesSearchQueryChange,
                            modifier = Modifier.widthIn(max = 220.dp).padding(end = 8.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.primary,
                ),
            )
        },
        bottomBar = {
            if (showBottomNav) {
                AppRootBottomNav(
                    rootTabs = rootTabs,
                    currentRoute = currentRoute,
                    onTabClick = { route -> navigateToRootTab(backStack, route) },
                )
            }
        },
    ) { innerPadding ->
        AppRootNavDisplay(
            navDeps = navDeps,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        )
    }
}

@Composable
private fun AppRootNavigationRail(
    rootTabs: List<RootTabItem>,
    currentRoute: NavKey?,
    onTabClick: (RootRoute) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    NavigationRail(
        containerColor = colors.surfaceContainerLow,
        header = {
            Icon(
                Icons.Default.Terminal,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.padding(vertical = 16.dp).size(24.dp),
            )
        },
    ) {
        rootTabs.forEach { tab ->
            NavigationRailItem(
                selected = currentRoute == tab.route,
                onClick = { onTabClick(tab.route) },
                icon = {
                    Icon(
                        tab.icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
                label = { Text(stringResource(tab.labelRes)) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = colors.onPrimaryContainer,
                    selectedTextColor = colors.onSurface,
                    indicatorColor = colors.primaryContainer,
                    unselectedIconColor = colors.onSurfaceVariant,
                    unselectedTextColor = colors.onSurfaceVariant,
                ),
            )
        }
    }
}

@Composable
private fun AppRootFilesSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(Res.string.files_search_placeholder)) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        singleLine = true,
        shape = AppControlShape,
        modifier = modifier,
    )
}

@Composable
private fun AppRootBottomNav(
    rootTabs: List<RootTabItem>,
    currentRoute: NavKey?,
    onTabClick: (RootRoute) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    NavigationBar(
        containerColor = colors.surfaceContainerLowest,
        tonalElevation = 0.dp,
    ) {
        rootTabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onTabClick(tab.route) },
                icon = {
                    Icon(
                        tab.icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
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
}

@Composable
private fun AppRootNavDisplay(
    navDeps: AppRootNavDependencies,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        backStack = navDeps.backStack,
        onBack = { navigateBackFromRoot(navDeps.backStack) },
        modifier = modifier,
        entryProvider = entryProvider {
            entry<RootRoute.Translate> {
                MainDashboardScreen(
                    settings = navDeps.settingsRepository,
                    services = navDeps.services,
                    xmlFileAccess = navDeps.xmlFileAccess,
                    projectRepository = navDeps.projectRepository,
                    compareProjectRepository = navDeps.compareProjectRepository,
                    resMultiProjectRepository = navDeps.resMultiProjectRepository,
                    editorControllerStore = navDeps.editorControllerStore,
                    onNavigateToFiles = { navigateToRootTab(navDeps.backStack, RootRoute.Files) },
                )
            }
            entry<RootRoute.Files> {
                FilesScreen(
                    settings = navDeps.settingsRepository,
                    services = navDeps.services,
                    xmlFileAccess = navDeps.xmlFileAccess,
                    projectRepository = navDeps.projectRepository,
                    editorControllerStore = navDeps.editorControllerStore,
                    searchQuery = navDeps.filesSearchQuery,
                )
            }
            entry<RootRoute.Settings> {
                SettingsScreen(repository = navDeps.settingsRepository)
            }
            entry<RootRoute.About> {
                AboutScreen()
            }
        },
    )
}

private enum class AppNavigationLayout {
    Compact,
    Medium,
    Expanded,
}

private fun appNavigationLayout(maxWidth: Dp, maxHeight: Dp): AppNavigationLayout =
    when {
        maxWidth >= 840.dp -> AppNavigationLayout.Expanded
        maxWidth >= 768.dp && maxHeight >= 600.dp -> AppNavigationLayout.Expanded
        maxWidth >= 600.dp -> AppNavigationLayout.Medium
        else -> AppNavigationLayout.Compact
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
