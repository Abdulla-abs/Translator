# 主页（main_page）Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 按任务顺序执行。步骤使用 `- [ ]` 勾选跟踪。

**Goal:** 用 [main_page 设计 spec](../specs/2026-05-19-main-page-design.md) 替换当前 `TranslateScreen` 默认首页，实现 Dashboard（Quick Translate + File Projects + 洞察区），并将底栏扩展为 4 项导航。

**Architecture:** 在 `commonMain` 新增 `MainDashboardScreen` 及 `screens/main/*` 子组件；复用 `TranslationServices`、`AppSettingsRepository`、`XmlFileAccess` 与既有 `ui/theme`、`ui/components`；文件列表首版用内存 `RecentXmlProjectRepository`；`TranslateFileTab` 迁至 `FilesScreen` 供「文件」路由使用。

**Tech Stack:** Compose Multiplatform、Material3、`AppTheme`、Navigation3、`WindowSizeClass`（可选宽屏 Rail）。

**设计输入:** [androidPages/main_page/code.html](../../../androidPages/main_page/code.html)、[screen.png](../../../androidPages/main_page/screen.png)

---

## 文件结构

| 路径 | 职责 |
|------|------|
| `composeApp/.../ui/screens/MainDashboardScreen.kt` | 主页滚动容器、组装各 section、接收导航回调 |
| `composeApp/.../ui/screens/main/QuickTranslateSection.kt` | Quick Translate 双栏 + 翻译按钮 |
| `composeApp/.../ui/screens/main/FileProjectsSection.kt` | 文件卡片网格、上传卡、VIEW ALL |
| `composeApp/.../ui/screens/main/DashboardInsightSection.kt` | Efficiency + Smart Sync 静态区 |
| `composeApp/.../ui/screens/main/MainDashboardModels.kt` | `RecentXmlProject` 数据类 |
| `composeApp/.../ui/screens/main/RecentXmlProjectRepository.kt` | 内存最近项目存储 |
| `composeApp/.../ui/screens/FilesScreen.kt` | 「文件」路由；首版包装原 `TranslateFileTab` |
| `composeApp/.../ui/components/AppGlassCard.kt` | mockup `glass-card` 样式 |
| `composeApp/.../ui/components/AppLanguageChip.kt` | SOURCE/TARGET 圆角语言 Chip |
| `composeApp/.../ui/components/FileProjectCard.kt` | 单文件项目卡片 + 进度 |
| `composeApp/.../ui/navigation/RootRoutes.kt` | 新增 `RootRoute.Files` + 序列化注册 |
| `composeApp/.../ui/AppRoot.kt` | 4 Tab、顶栏设置按钮、Dashboard/FAB、路由表 |
| `composeApp/.../ui/screens/TranslateScreens.kt` | 删除或仅保留 `TranslateFileTab` 供 `FilesScreen` import |
| `composeApp/build.gradle.kts` | 可选：`material-icons-extended` |

---

### Task 1: 导航与路由扩展

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/ui/navigation/RootRoutes.kt`
- Modify: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/ui/AppRoot.kt`

- [ ] **Step 1: 在 `RootRoute` 增加 `Files`**

```kotlin
@Serializable
data object Files : RootRoute
```

在 `rootNavSavedStateConfig` 的 `polymorphic` 中注册 `RootRoute.Files.serializer()`。

- [ ] **Step 2: 更新 `rootTabs` 为 4 项**

顺序与 mockup 一致：`Translate`（译）、`Files`（件）、`Settings`（设）、`About`（关）。

- [ ] **Step 3: `NavDisplay` 增加 `Files` entry**

暂指向 `FilesScreen(...)`；`Translate` entry 改为 `MainDashboardScreen(...)`。

- [ ] **Step 4: 顶栏右侧增加设置按钮**

```kotlin
IconButton(onClick = { navigateToRootTab(backStack, RootRoute.Settings) }) {
    Icon(Icons.Default.Settings, contentDescription = "设置")
}
```

- [ ] **Step 5: 编译验证**

```bash
.\gradlew.bat :composeApp:compileKotlinJvm
```

Expected: BUILD SUCCESSFUL

---

### Task 2: 设计组件（Glass / Chip / FileCard）

**Files:**
- Create: `composeApp/.../ui/components/AppGlassCard.kt`
- Create: `composeApp/.../ui/components/AppLanguageChip.kt`
- Create: `composeApp/.../ui/components/FileProjectCard.kt`
- Modify: `composeApp/build.gradle.kts`（若尚无 icons）

- [ ] **Step 1: 添加 Material Icons Extended（若无）**

`commonMain.dependencies` 增加：

```kotlin
implementation(compose.materialIconsExtended)
```

- [ ] **Step 2: 实现 `AppGlassCard`**

```kotlin
@Composable
fun AppGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppCardShape,
        color = colors.surfaceContainer.copy(alpha = 0.85f),
        border = BorderStroke(AppOutlineStrokeWidth.dp, colors.outlineVariant.copy(alpha = 0.5f)),
        content = { Column(Modifier.padding(AppSpacing.lg), content = content) },
    )
}
```

- [ ] **Step 3: 实现 `AppLanguageChip`**

显示语言码；`onClick` 展开为 `AppOutlinedField` 单行或 `AlertDialog` 输入（首版可用 `OutlinedTextField` 替换 Chip 编辑态）。

- [ ] **Step 4: 实现 `FileProjectCard`**

参数：`RecentXmlProject`、`onClick`；含左侧 4dp 色条、`AppThinProgress(progress)`、Badge 百分比、`code-sm` 副标题。

- [ ] **Step 5: 编译验证**

```bash
.\gradlew.bat :composeApp:compileKotlinJvm
```

---

### Task 3: 最近文件数据层

**Files:**
- Create: `composeApp/.../ui/screens/main/MainDashboardModels.kt`
- Create: `composeApp/.../ui/screens/main/RecentXmlProjectRepository.kt`

- [ ] **Step 1: 定义模型**

```kotlin
data class RecentXmlProject(
    val id: String,
    val displayName: String,
    val modifiedAtEpochMs: Long,
    val progressPercent: Float, // 0f..1f
    val translatedKeys: Int,
    val totalKeys: Int,
    val isComplete: Boolean,
)
```

- [ ] **Step 2: 内存仓库**

```kotlin
class InMemoryRecentXmlProjectRepository {
    private val _projects = MutableStateFlow<List<RecentXmlProject>>(emptyList())
    val projects: StateFlow<List<RecentXmlProject>> = _projects.asStateFlow()

    fun addOrUpdate(project: RecentXmlProject) { /* prepend, max 10 */ }
}
```

- [ ] **Step 3: 从 XML 估算 key 数（可选）**

上传后调用 `StringsXmlCodec` 解析统计 `totalKeys`；`translatedKeys = 0`，`progressPercent = 0f`。

- [ ] **Step 4: 单测（可选最小）**

`commonTest` 中测试 `addOrUpdate` 保留最多 10 条、按 id 更新。

```kotlin
@Test
fun addOrUpdate_keepsMaxTen() {
    val repo = InMemoryRecentXmlProjectRepository()
    repeat(12) { i ->
        repo.addOrUpdate(sampleProject("id$i"))
    }
    assertEquals(10, repo.projects.value.size)
}
```

```bash
.\gradlew.bat :composeApp:jvmTest --tests "*RecentXmlProject*"
```

---

### Task 4: QuickTranslateSection

**Files:**
- Create: `composeApp/.../ui/screens/main/QuickTranslateSection.kt`

- [ ] **Step 1: 从 `TranslateTextTab` 迁移状态与逻辑**

参数：`services: TranslationServices`、`defaultFrom: String`、`defaultTo: String`。

状态：`from`、`to`、`input`、`output`、`error`、`loading`。

- [ ] **Step 2: 布局对照 mockup**

`AppGlassCard` 内 `Row`（宽屏）/ `Column`（窄屏）：
- 左：SOURCE 标签 + `AppLanguageChip` + 等宽 `AppOutlinedField`（`useMonospace = true`，minLines=4）
- 右：TARGET 标签 + Chip + 只读结果区（空态居中图标 `Icons.Default.Code` + 占位文案）+ `AppPrimaryButton`「翻译」

- [ ] **Step 3: 加载与错误**

`loading` 时 `AppThinProgress(inProgress = true)`；错误 `MaterialTheme.colorScheme.error`。

- [ ] **Step 4: 手动验证清单**

- 输入英文句子 → 翻译 → 译文出现在 TARGET 区
- 空输入时按钮 disabled

---

### Task 5: FileProjectsSection

**Files:**
- Create: `composeApp/.../ui/screens/main/FileProjectsSection.kt`

- [ ] **Step 1: Section 标题行**

`Icons.Default.FolderZip` +「File Projects」+ TextButton「VIEW ALL」→ `onViewAllClick`。

- [ ] **Step 2: 响应式网格**

```kotlin
val columns = when {
    maxWidth >= 840.dp -> 3
    maxWidth >= 600.dp -> 2
    else -> 1
}
LazyVerticalGrid(columns = GridCells.Fixed(columns), ...)
```

- [ ] **Step 3: 上传虚线卡**

`Modifier.border(2.dp, dashed?, colors.outlineVariant)` + `Icons.Default.UploadFile` + 文案；`clickable` → `onUploadClick`。

- [ ] **Step 4: 绑定 `RecentXmlProjectRepository`**

`projects.collectAsState()` 渲染 `FileProjectCard`；无数据时仅显示上传卡。

- [ ] **Step 5: 上传回调**

`onUploadClick` 由父级调用 `xmlFileAccess.launchPickXml`；成功则 `repository.addOrUpdate(...)`。

---

### Task 6: DashboardInsightSection

**Files:**
- Create: `composeApp/.../ui/screens/main/DashboardInsightSection.kt`

- [ ] **Step 1: 实现静态双卡**

左卡（占 3/4 宽）：`Efficiency +24%`（`displayLarge` 或缩小为 32sp 移动端）+ body 说明；背景 `Brush.verticalGradient`。

右卡：`tertiaryContainer` 色 `Smart Sync` + `Icons.Default.AutoAwesome`。

- [ ] **Step 2: 响应式**

窄屏改为上下堆叠 `Column`。

---

### Task 7: MainDashboardScreen 组装

**Files:**
- Create: `composeApp/.../ui/screens/MainDashboardScreen.kt`
- Modify: `composeApp/.../ui/AppRoot.kt`

- [ ] **Step 1: 主页 Scaffold 内容**

```kotlin
@Composable
fun MainDashboardScreen(
    settings: AppSettingsRepository,
    services: TranslationServices,
    xmlFileAccess: XmlFileAccess,
    projectRepository: InMemoryRecentXmlProjectRepository,
    onNavigateToFiles: () -> Unit,
    onOpenProject: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snap by settings.snapshot.collectAsState()
    val scroll = rememberScrollState()
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.verticalScroll(scroll).padding(AppSpacing.gutter)) {
            QuickTranslateSection(services, snap.defaultSourceLang, snap.defaultTargetLang)
            Spacer(Modifier.height(AppSpacing.lg))
            FileProjectsSection(...)
            Spacer(Modifier.height(AppSpacing.lg))
            DashboardInsightSection()
            Spacer(Modifier.height(80.dp)) // 底栏留白
        }
        // 紧凑宽度 FAB
        if (isCompactWidth) {
            FloatingActionButton(
                onClick = { /* launchPickXml */ },
                modifier = Modifier.align(Alignment.BottomEnd).padding(AppSpacing.margin),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) { Icon(Icons.Default.Add, ...) }
        }
    }
}
```

- [ ] **Step 2: `AppRoot` 注入 `remember { InMemoryRecentXmlProjectRepository() }`**

- [ ] **Step 3: 删除对 `TranslateScreen` 的引用**

---

### Task 8: FilesScreen 与 TranslateScreens 收尾

**Files:**
- Create: `composeApp/.../ui/screens/FilesScreen.kt`
- Modify: `composeApp/.../ui/screens/TranslateScreens.kt`

- [ ] **Step 1: `FilesScreen` 包装 `TranslateFileTab`**

```kotlin
@Composable
fun FilesScreen(
    settings: AppSettingsRepository,
    services: TranslationServices,
    xmlFileAccess: XmlFileAccess,
    modifier: Modifier = Modifier,
) {
    val snap by settings.snapshot.collectAsState()
    TranslateFileTab(services, snap, xmlFileAccess)
}
```

将 `TranslateFileTab` 改为 `internal` 或移至 `FilesScreen.kt` 同文件。

- [ ] **Step 2: 删除 `TranslateScreen` 与 `TranslateTextTab`**

避免重复入口；确认无其他引用。

- [ ] **Step 3: 全量测试**

```bash
.\gradlew.bat :composeApp:compileKotlinJvm :composeApp:jvmTest
```

Expected: BUILD SUCCESSFUL

---

### Task 9: 宽屏 NavigationRail（可选，时间允许时）

**Files:**
- Modify: `composeApp/.../ui/AppRoot.kt`

- [ ] **Step 1: `BoxWithConstraints` 判断 `maxWidth > 840.dp`**

- [ ] **Step 2: 左侧 `NavigationRail` 4 项，与底栏互斥显示**

对照 `code.html` 侧栏：选中项 `secondaryContainer` 背景。

- [ ] **Step 3: 桌面编译运行目测**

```bash
.\gradlew.bat :composeApp:run
```

---

## Spec 覆盖自检

| Spec 要求 | 任务 |
|-----------|------|
| Quick Translate 真实翻译 | Task 4 |
| File Projects 上传 + 列表 | Task 3, 5 |
| VIEW ALL / 文件路由 | Task 1, 8 |
| 洞察区静态 | Task 6 |
| 4 项底栏 + 设置入口 | Task 1, 7 |
| AppTheme 令牌 | Task 2（复用既有 theme） |
| 非目标：真实统计/Smart Sync | 不实现 |

---

## 执行后手动验收（桌面 + Android）

1. 默认页为 Dashboard，无「文本|文件」顶 Tab。
2. Quick Translate 翻译成功/失败提示正确。
3. 上传 XML 后出现文件卡；VIEW ALL 进入文件页。
4. 底栏切换：文件 / 设置 / 关于 / 返回翻译。
5. 顶栏设置图标进入设置页。
