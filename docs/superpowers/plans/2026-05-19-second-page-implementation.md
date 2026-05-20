# 文件浏览页（second_page）Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 按任务顺序执行。步骤使用 `- [ ]` 勾选跟踪。

**Goal:** 用 [second_page 设计 spec](../specs/2026-05-19-second-page-design.md) 替换 `RootRoute.Files` 下当前 `TranslateFileTab` 占位，实现文件浏览列表（面包屑、搜索、Filter、文件夹/XML 行、FAB 上传），VIEW 暂进内嵌详情（`TranslateFileTab`）。

**Architecture:** `screens/files/` 下 `FileBrowserStore`（内存虚拟树 + 合并 `RecentXmlProjectRepository`）+ 组合式 UI；`FilesScreen` 负责 `Browse | Detail` 模式切换；`AppRoot` 在 Files 路由时扩展顶栏搜索；`TranslateFileTab` 增加可选 `initialSourceXml` 参数供详情预填。

**Tech Stack:** Compose Multiplatform、Material3、`AppTheme`、`material-icons-extended`、既有 `XmlFileAccess` / `RecentXmlProjectRepository`。

**设计输入:** [androidPages/second_page/code.html](../../../androidPages/second_page/code.html)、[screen.png](../../../androidPages/second_page/screen.png)

---

## 文件结构

| 路径 | 职责 |
|------|------|
| `composeApp/.../ui/screens/files/FileBrowserModels.kt` | `FileBrowserItem`、`FileBrowserState` |
| `composeApp/.../ui/screens/files/InMemoryFileBrowserStore.kt` | 虚拟目录、导航、搜索过滤、合并最近项目 |
| `composeApp/.../ui/screens/files/FileBreadcrumbBar.kt` | 面包屑 + Home + Filter |
| `composeApp/.../ui/screens/files/FileListSection.kt` | 玻璃卡片 + 表头 + 行列表 |
| `composeApp/.../ui/screens/files/FileBrowserRow.kt` | Folder / XmlFile 单行 |
| `composeApp/.../ui/screens/files/FileBrowserScreen.kt` | Browse 模式主 UI + FAB |
| `composeApp/.../ui/screens/FilesScreen.kt` | `Browse`/`Detail` 切换、组装依赖 |
| `composeApp/.../ui/screens/TranslateScreens.kt` | `TranslateFileTab(initialSourceXml: String? = null)` |
| `composeApp/.../ui/AppRoot.kt` | Files 路由顶栏搜索；向 Files 传入 `projectRepository` |
| `composeApp/.../commonTest/.../InMemoryFileBrowserStoreTest.kt` | 路径导航与过滤单测 |

---

### Task 1: 文件浏览数据层

**Files:**
- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/ui/screens/files/FileBrowserModels.kt`
- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/ui/screens/files/InMemoryFileBrowserStore.kt`
- Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/ui/screens/files/InMemoryFileBrowserStoreTest.kt`

- [ ] **Step 1: 定义模型**

```kotlin
package `fun`.abbas.android_res_translator.ui.screens.files

sealed interface FileBrowserItem {
    data class Folder(val name: String, val path: String) : FileBrowserItem
    data class XmlFile(
        val id: String,
        val name: String,
        val path: String,
        val versionLabel: String,
        val sizeLabel: String,
        val xmlContent: String?,
    ) : FileBrowserItem
}

data class FileBrowserState(
    val pathSegments: List<String> = emptyList(),
    val items: List<FileBrowserItem> = emptyList(),
    val searchQuery: String = "",
)
```

- [ ] **Step 2: 实现 `InMemoryFileBrowserStore`**

核心 API：

```kotlin
class InMemoryFileBrowserStore(
    private val recentProjects: InMemoryRecentXmlProjectRepository,
) {
    private val _state = MutableStateFlow(FileBrowserState())
    val state: StateFlow<FileBrowserState> = _state.asStateFlow()

    fun navigateHome()
    fun navigateToSegment(index: Int) // 面包屑点击
    fun enterFolder(folder: FileBrowserItem.Folder)
    fun setSearchQuery(query: String)
    fun refresh() // 重建 items：mock 树 + recentProjects
    fun xmlContentFor(id: String): String?
}
```

**Mock 树逻辑（固定）**：

| `pathSegments` | 显示 items |
|----------------|------------|
| `[]` | 仅 `Folder("src", "src")` |
| `["src"]` | `Folder("commonMain", "src/commonMain")` |
| `["src","commonMain"]` | `Folder("resources", "src/commonMain/resources")` |
| `["src","commonMain","resources"]` | `Folder("layout", ...)` + 3 个 mock XML + `recentProjects` 映射的 XML |

`sizeLabel`：`"${(content.length / 1024).coerceAtLeast(1)} KB"` 或 `"—"`。

- [ ] **Step 3: 单测**

```kotlin
@Test
fun navigateHome_resetsToRoot() {
    val store = InMemoryFileBrowserStore(InMemoryRecentXmlProjectRepository())
    store.enterFolder(FileBrowserItem.Folder("src", "src"))
    store.navigateHome()
    assertEquals(emptyList(), store.state.value.pathSegments)
}

@Test
fun searchQuery_filtersByName() {
    val store = InMemoryFileBrowserStore(InMemoryRecentXmlProjectRepository())
    // 导航到 resources，setSearchQuery("strings_main")
    // assert items 仅含 strings_main.xml
}
```

Run: `.\gradlew.bat :composeApp:jvmTest --tests "*InMemoryFileBrowserStore*"`  
Expected: PASS

---

### Task 2: 列表与面包屑 UI 组件

**Files:**
- Create: `composeApp/.../ui/screens/files/FileBreadcrumbBar.kt`
- Create: `composeApp/.../ui/screens/files/FileBrowserRow.kt`
- Create: `composeApp/.../ui/screens/files/FileListSection.kt`

- [ ] **Step 1: `FileBreadcrumbBar`**

参数：`pathSegments`、`onHomeClick`、`onSegmentClick(index)`、`onFilterClick`（首版 toggle 本地 filter 模式可选）。

布局：横向 `LazyRow`；Home 圆形按钮 `Icons.Default.Home`；`ChevronRight` 分隔；分段 Chip（当前段 `primary` 边框 + `FolderOpen` 图标）。

- [ ] **Step 2: `FileBrowserRow`**

- **Folder**：`Icons.Default.Folder`（tertiary 色）、名称 + "Directory"、右侧 `ChevronRight`。
- **XmlFile**：`Icons.Default.Description`、`code-base` 文件名、副标题 `"${versionLabel} • ${sizeLabel}"`、可选 `Download` 图标（`Modifier.size(0.dp)` 隐藏于窄屏用 `BoxWithConstraints`）、**VIEW** 按钮（`AppPrimaryButton` 缩小样式或 `TextButton` + `primaryContainer`）。

- [ ] **Step 3: `FileListSection`**

`AppGlassCard` 内：
- 表头行 `NAME` | `ACTION`（`AppSectionTitle` / `label-caps`）
- `Column` + `HorizontalDivider` 分隔各行
- 传入 `items: List<FileBrowserItem>` 与回调

- [ ] **Step 4: 编译**

```bash
.\gradlew.bat :composeApp:compileKotlinJvm
```

---

### Task 3: FileBrowserScreen 组装

**Files:**
- Create: `composeApp/.../ui/screens/files/FileBrowserScreen.kt`

- [ ] **Step 1: 实现 Browse 屏**

```kotlin
@Composable
fun FileBrowserScreen(
    store: InMemoryFileBrowserStore,
    xmlFileAccess: XmlFileAccess,
    onViewFile: (FileBrowserItem.XmlFile) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by store.state.collectAsState()
    Box(modifier) {
        Column(Modifier.verticalScroll(...).padding(AppSpacing.gutter)) {
            FileBreadcrumbBar(...)
            Spacer(...)
            FileListSection(
                items = state.items,
                onFolderClick = { store.enterFolder(it) },
                onViewClick = onViewFile,
            )
        }
        FloatingActionButton(
            modifier = Modifier.align(Alignment.BottomEnd).padding(AppSpacing.margin),
            onClick = { xmlFileAccess.launchPickXml { r -> r.onSuccess { xml -> ... store.refresh() } } },
        ) { Icon(Icons.Default.UploadFile, ...) }
    }
}
```

上传成功：调用既有 `recentProjectFromXml` + `recentProjects.addOrUpdate` + `store.refresh()`。

- [ ] **Step 2: 将 `searchQuery` 与 AppRoot 顶栏联动（见 Task 4）**

`store.setSearchQuery` 由外部传入或 `LaunchedEffect(searchQuery)` 同步。

---

### Task 4: FilesScreen 与 AppRoot 接线

**Files:**
- Modify: `composeApp/.../ui/screens/FilesScreen.kt`
- Modify: `composeApp/.../ui/AppRoot.kt`
- Modify: `composeApp/.../ui/screens/TranslateScreens.kt`

- [ ] **Step 1: `FilesScreen` 模式切换**

```kotlin
@Composable
fun FilesScreen(
    settings: AppSettingsRepository,
    services: TranslationServices,
    xmlFileAccess: XmlFileAccess,
    projectRepository: InMemoryRecentXmlProjectRepository,
    searchQuery: String,
    modifier: Modifier = Modifier,
) {
    val store = remember(projectRepository) { InMemoryFileBrowserStore(projectRepository) }
    LaunchedEffect(searchQuery) { store.setSearchQuery(searchQuery) }
    var mode by remember { mutableStateOf<FilesUiMode>(FilesUiMode.Browse) }
    val snap by settings.snapshot.collectAsState()

    when (val m = mode) {
        FilesUiMode.Browse -> FileBrowserScreen(
            store = store,
            xmlFileAccess = xmlFileAccess,
            onViewFile = { mode = FilesUiMode.Detail(it.id) },
            modifier = modifier,
        )
        is FilesUiMode.Detail -> {
            // 顶栏返回由 FilesScreen 或 AppRoot 提供
            TranslateFileTab(
                services = services,
                snapshot = snap,
                xmlFileAccess = xmlFileAccess,
                initialSourceXml = store.xmlContentFor(m.fileId),
                onBack = { mode = FilesUiMode.Browse },
                modifier = modifier,
            )
        }
    }
}

private sealed interface FilesUiMode {
    data object Browse : FilesUiMode
    data class Detail(val fileId: String) : FilesUiMode
}
```

- [ ] **Step 2: `TranslateFileTab` 支持预填与返回**

```kotlin
@Composable
internal fun TranslateFileTab(
    services: TranslationServices,
    snapshot: AppSettingsSnapshot,
    xmlFileAccess: XmlFileAccess,
    modifier: Modifier = Modifier,
    initialSourceXml: String? = null,
    onBack: (() -> Unit)? = null,
) {
    var sourceXml by remember(initialSourceXml) { mutableStateOf(initialSourceXml.orEmpty()) }
    // 若 onBack != null，Column 顶部加 IconButton(Icons.AutoMirrored.Filled.ArrowBack)
}
```

Mock 文件无 `xmlContent` 时 VIEW 打开空编辑器，用户可自行粘贴或「从文件读取」。

- [ ] **Step 3: `AppRoot` 传入 `projectRepository`；Files 路由搜索框**

```kotlin
// AppRoot 内
var filesSearchQuery by remember { mutableStateOf("") }

// topBar actions 或 custom topBar：
if (backStack.lastOrNull() == RootRoute.Files) {
    OutlinedTextField(
        value = filesSearchQuery,
        onValueChange = { filesSearchQuery = it },
        placeholder = { Text("Search files...") },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        modifier = Modifier.widthIn(max = 280.dp),
        singleLine = true,
    )
}

entry<RootRoute.Files> {
    FilesScreen(
        ...
        projectRepository = projectRepository,
        searchQuery = filesSearchQuery,
    )
}
```

窄屏：搜索框可收到 `FileBrowserScreen` 内第二行（`maxWidth < 600.dp`）以避免顶栏拥挤。

- [ ] **Step 4: 编译 + 测试**

```bash
.\gradlew.bat :composeApp:compileKotlinJvm :composeApp:jvmTest
```

Expected: BUILD SUCCESSFUL

---

### Task 5: 与主页数据打通

**Files:**
- Modify: `composeApp/.../ui/screens/MainDashboardScreen.kt`（如需，确保上传与 Files 共用 repository — 已在 AppRoot 单例，无需改）
- Modify: `composeApp/.../ui/screens/files/InMemoryFileBrowserStore.kt`

- [ ] **Step 1: `RecentXmlProject` → `XmlFile` 映射**

```kotlin
private fun RecentXmlProject.toBrowserItem(pathPrefix: String): FileBrowserItem.XmlFile =
    FileBrowserItem.XmlFile(
        id = id,
        name = displayName,
        path = "$pathPrefix/$displayName",
        versionLabel = "v1.0",
        sizeLabel = "${totalKeys} keys",
        xmlContent = null, // 若需 VIEW 带内容，扩展 RecentXmlProject 存 xml 或 id→content map
    )
```

- [ ] **Step 2: 扩展 `RecentXmlProject` 或仓库保存 `xmlContent`（推荐）**

在 `RecentXmlProject` 增加 `val sourceXml: String`（上传时写入），VIEW 时 `xmlContentFor` 返回真实内容。

修改：
- `MainDashboardModels.kt`：`sourceXml: String = ""`
- `XmlProjectFactory.kt`：写入 `sourceXml = xml`
- `InMemoryFileBrowserStore.xmlContentFor`：从 recent 项读取

- [ ] **Step 3: 更新 `RecentXmlProjectRepositoryTest` / 工厂调用处**

Run: `.\gradlew.bat :composeApp:jvmTest`

---

### Task 6: 手动验收清单

- [ ] 底栏「文件」→ 面包屑 + 列表（非旧表单独占屏）
- [ ] 点击 `layout` 文件夹下钻；Home 回根
- [ ] 搜索 `strings` 过滤列表
- [ ] VIEW → 详情页有返回；mock 文件可编辑
- [ ] FAB 上传 → `resources` 下出现新条目；主页 File Projects 同步
- [ ] 桌面 `.\gradlew.bat :composeApp:run` 目测

---

## Spec 覆盖自检

| Spec 要求 | Task |
|-----------|------|
| 替换 TranslateFileTab 列表占位 | Task 3–4 |
| 面包屑 + 文件夹下钻 | Task 1–2 |
| 搜索 / Filter | Task 2–4（Filter 首版同搜索或 no-op 按钮） |
| VIEW → 详情 | Task 4–5 |
| FAB 上传 | Task 3 |
| 共享 RecentXmlProject | Task 5 |
| file_page 完整 UI | 不在本计划 |

---

## 与后续 file_page 的衔接

`FilesUiMode.Detail` 在 `file_page` 计划完成后替换为 `FileEditorScreen`（三栏/分段控件布局），保留 `FileBrowserStore.xmlContentFor(id)` 作为数据入口。
