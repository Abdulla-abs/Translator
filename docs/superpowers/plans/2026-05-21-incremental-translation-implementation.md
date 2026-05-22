# 增量 / 全量翻译模式 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在文件翻译工作台落地「增量 / 全量」双模式：Dashboard 双上传区、双文件增量创建、全量详情页补目标文件；核心 `Filled` 支持空值与 array 按 item 增量；删除 `TranslateFileTab` 与设置页合并策略。

**Architecture:** 抽取 `IncrementalSlotPolicy` 统一 `FilledTranslationConsumer` 与 `IncrementalTranslationPlanner`；`RecentXmlProject.workflowMode` 绑定 Consumer（增量→Filled，全量→AllReplace）；`target-baseline.xml` 持久化；`FileEditorController` 分 `loadIncremental` / `loadFull` / `awaitTarget` 三态。

**Tech Stack:** Kotlin Multiplatform、Compose Multiplatform、Material3、`StringsXmlCodec`、`TranslationProjectFileStore`、既有 `FileEditorScreen`。

**设计输入:** [2026-05-21-incremental-translation-design.md](../specs/2026-05-21-incremental-translation-design.md)

---

## 文件结构

| 路径 | 职责 |
|------|------|
| `core/resources/consumer/IncrementalSlotPolicy.kt` | **新建** string / array item 是否需译 |
| `core/resources/consumer/FilledTranslationConsumer.kt` | 改用 Policy；array 按 item |
| `core/resources/consumer/AllReplaceTranslationConsumer.kt` | 不变（全量覆盖） |
| `core/resources/planner/IncrementalTranslationPlanner.kt` | **新建** 生成 `PlannedEntry` / `XmlEntryUi` 列表 |
| `core/resources/planner/TranslationWorkflowMode.kt` | **新建** `INCREMENTAL` \| `FULL` |
| `ui/screens/main/MainDashboardModels.kt` | `RecentXmlProject` 扩展字段 |
| `persistence/TranslationProjectIndex.kt` | index 序列化扩展 |
| `persistence/TranslationProjectFileStore.kt` | `target-baseline.xml` 读写、`createIncrementalProject` |
| `ui/screens/main/TranslationProjectRepository.kt` | `addIncrementalFromUpload` / `addFullFromUpload` |
| `ui/screens/main/FileProjectsSection.kt` | 双 Upload 卡片 |
| `ui/screens/MainDashboardScreen.kt` | 双文件增量 / 单文件全量创建 |
| `ui/screens/fileeditor/FileEditorModels.kt` | `EntryStatus.Skipped`、`skippedCount` |
| `ui/screens/fileeditor/FileEditorController.kt` | `loadIncremental` / `attachTarget` / `canTranslate` |
| `ui/screens/fileeditor/FileEditorScreen.kt` | 模式 Badge、全量 target Banner |
| `ui/screens/fileeditor/FileEditorProgressSection.kt` | Skipped 统计 |
| `ui/screens/fileeditor/FileEditorControllerStore.kt` | 传入 `workflowMode`、`targetXml` |
| `ui/screens/settings/SettingsComponents.kt` | 删除 `MergeStrategyRow` |
| `ui/screens/TranslateScreens.kt` | **删除** `TranslateFileTab`（整文件若仅剩则删文件） |
| `composeResources/values/strings.xml` + `values-zh` | 新文案键 |
| `commonTest/.../IncrementalSlotPolicyTest.kt` | Policy 单测 |
| `commonTest/.../IncrementalTranslationPlannerTest.kt` | Planner 单测 |
| `commonTest/.../FilledTranslationConsumerTest.kt` | 扩展空值、array item |
| `commonTest/.../FileEditorControllerTest.kt` | 全量无 target、增量 Skipped |

---

## Task 1: `IncrementalSlotPolicy`（核心规则）

**Files:**
- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/consumer/IncrementalSlotPolicy.kt`
- Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/resources/consumer/IncrementalSlotPolicyTest.kt`
- Modify: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/consumer/FilledTranslationConsumer.kt`

- [ ] **Step 1: 写失败测试**

```kotlin
// IncrementalSlotPolicyTest.kt
@Test fun string_missingKey_needsTranslate() {
    assertTrue(IncrementalSlotPolicy.needsTranslateString(target = null))
}
@Test fun string_blankValue_needsTranslate() {
    assertTrue(IncrementalSlotPolicy.needsTranslateString(
        target = StringEntry("k", "  ", translatable = true),
    ))
}
@Test fun string_nonBlank_skips() {
    assertFalse(IncrementalSlotPolicy.needsTranslateString(
        target = StringEntry("k", "Hi", translatable = true),
    ))
}
@Test fun array_existingWithOneBlankItem_needsArrayWork() {
    val src = StringArrayEntry("a", listOf("A", "B"))
    val tgt = StringArrayEntry("a", listOf("x", ""))
    assertTrue(IncrementalSlotPolicy.needsTranslateStringArray(src, tgt))
    assertFalse(IncrementalSlotPolicy.needsTranslateStringArrayItem("x"))
    assertTrue(IncrementalSlotPolicy.needsTranslateStringArrayItem(""))
}
```

- [ ] **Step 2: 运行测试确认 FAIL**

```bash
cd f:\idea_pro\AndroidResTranslator
.\gradlew :composeApp:jvmTest --tests "fun.abbas.android_res_translator.core.resources.consumer.IncrementalSlotPolicyTest" --no-daemon
```

Expected: FAIL（类未定义）

- [ ] **Step 3: 实现 Policy**

```kotlin
// IncrementalSlotPolicy.kt
object IncrementalSlotPolicy {
    fun needsTranslateString(target: StringEntry?): Boolean =
        target == null || target.value.isBlank()

    fun needsTranslateStringArray(
        source: StringArrayEntry,
        target: StringArrayEntry?,
    ): Boolean =
        target == null ||
            source.items.indices.any { i ->
                needsTranslateStringArrayItem(target.items.getOrNull(i))
            }

    fun needsTranslateStringArrayItem(targetItem: String?): Boolean =
        targetItem == null || targetItem.isBlank()
}
```

- [ ] **Step 4: 更新 `FilledTranslationConsumer`**

```kotlin
override fun shouldTranslateString(key: String, source: StringEntry, target: StringEntry?): Boolean =
    key in mustTranslateNames || IncrementalSlotPolicy.needsTranslateString(target)

override fun shouldTranslateStringArray(source: StringArrayEntry, target: StringArrayEntry?): Boolean =
    IncrementalSlotPolicy.needsTranslateStringArray(source, target)

override fun shouldTranslateStringArrayItem(sourceItem: String, targetItem: String?): Boolean =
    IncrementalSlotPolicy.needsTranslateStringArrayItem(targetItem)
```

- [ ] **Step 5: 扩展 `FilledTranslationConsumerTest`**

在 `filled_skipsExistingString` 后增加：

```kotlin
@Test fun filled_translatesBlankExistingString() = runTest {
    val source = StringResourceFile(strings = mapOf("k" to StringEntry("k", "Hello", true)))
    val target = StringResourceFile(strings = mapOf("k" to StringEntry("k", "", true)))
    val out = FilledTranslationConsumer().accept(source, "en", target, "zh", port)
    assertEquals("[Hello]", out.entries["k"])
}

@Test fun filled_partialArrayItemFill() = runTest {
    val source = StringResourceFile(stringArrays = mapOf("tabs" to StringArrayEntry("tabs", listOf("A", "B"))))
    val target = StringResourceFile(stringArrays = mapOf("tabs" to StringArrayEntry("tabs", listOf("已译", ""))))
    val out = FilledTranslationConsumer().accept(source, "en", target, "zh", port)
    assertEquals(listOf("已译", "[B]"), out.stringArrays["tabs"]?.items)
}
```

- [ ] **Step 6: 运行测试**

```bash
.\gradlew :composeApp:jvmTest --tests "fun.abbas.android_res_translator.core.resources.consumer.*" --no-daemon
```

Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/consumer/
git add composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/resources/consumer/
git commit -m "feat(core): align Filled consumer with incremental slot policy including blank values"
```

---

## Task 2: `IncrementalTranslationPlanner`

**Files:**
- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/planner/IncrementalTranslationPlanner.kt`
- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/planner/TranslationWorkflowMode.kt`
- Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/resources/planner/IncrementalTranslationPlannerTest.kt`

- [ ] **Step 1: 定义 `PlannedEntryAction` 与 Planner 测试**

```kotlin
enum class PlannedEntryAction { TRANSLATE, SKIP, SKIP_NOT_TRANSLATABLE }

data class PlannedStringEntry(
    val key: String,
    val sourceText: String,
    val targetText: String?,
    val action: PlannedEntryAction,
    val translatable: Boolean,
)

// PlannerTest: 源 3 key，目标 1 有值 1 空 1 缺失 → 待译 2，跳过 1
```

- [ ] **Step 2: 实现 `planStrings`（仅 string，array 首版可第二步补）**

```kotlin
object IncrementalTranslationPlanner {
    fun plan(
        source: StringResourceFile,
        target: StringResourceFile,
        forceTranslation: Boolean,
    ): List<PlannedStringEntry> =
        source.strings.map { (key, src) ->
            val tgt = target.strings[key]
            val skipNotTranslatable = !forceTranslation && !src.translatable
            val action =
                when {
                    skipNotTranslatable -> PlannedEntryAction.SKIP_NOT_TRANSLATABLE
                    key in /* mustTranslate */ emptySet() -> PlannedEntryAction.TRANSLATE
                    IncrementalSlotPolicy.needsTranslateString(tgt) -> PlannedEntryAction.TRANSLATE
                    else -> PlannedEntryAction.SKIP
                }
            PlannedStringEntry(
                key = key,
                sourceText = src.value,
                targetText = tgt?.value?.takeIf { it.isNotBlank() },
                action = action,
                translatable = src.translatable,
            )
        }
}
```

- [ ] **Step 3: 实现 `toXmlEntryUiList()` 扩展函数**

```kotlin
fun List<PlannedStringEntry>.toXmlEntryUiList(): List<XmlEntryUi> =
    map { p ->
        when (p.action) {
            PlannedEntryAction.TRANSLATE -> XmlEntryUi(
                key = p.key, sourceText = p.sourceText, targetText = null,
                status = EntryStatus.Pending, translatable = true,
            )
            PlannedEntryAction.SKIP -> XmlEntryUi(
                key = p.key, sourceText = p.sourceText, targetText = p.targetText,
                status = EntryStatus.Skipped, translatable = true,
            )
            PlannedEntryAction.SKIP_NOT_TRANSLATABLE -> XmlEntryUi(
                key = p.key, sourceText = p.sourceText, targetText = p.sourceText,
                status = EntryStatus.Skipped, translatable = false,
            )
        }
    }
```

- [ ] **Step 4: 运行 Planner 测试 + Commit**

```bash
.\gradlew :composeApp:jvmTest --tests "fun.abbas.android_res_translator.core.resources.planner.*" --no-daemon
git commit -m "feat(core): add IncrementalTranslationPlanner for editor entry lists"
```

---

## Task 3: 删除遗留 UI + 设置合并策略

**Files:**
- Delete or gut: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/ui/screens/TranslateScreens.kt`
- Modify: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/ui/screens/settings/SettingsComponents.kt`
- Modify: `composeApp/src/commonMain/composeResources/values/strings.xml`
- Modify: `composeApp/src/commonMain/composeResources/values-zh/strings.xml`

- [ ] **Step 1: 确认无引用**

```bash
rg "TranslateFileTab|TranslateScreens" composeApp/src --glob "*.kt"
```

Expected: 仅 `TranslateScreens.kt` 自身

- [ ] **Step 2: 删除 `TranslateScreens.kt`**

若文件仅含 `TranslateFileTab`，删除整个文件。

- [ ] **Step 3: 从 `SettingsStrategiesCard` 移除 `MergeStrategyRow` 调用及相关 private Composable**

删除 `MergeStrategyRow`、`MergeStrategyOption`（若存在），保留 `UiLocaleRow`、`AppearanceThemeRow`、`StrategyToggleRow`（forceTranslation）、`LocalizationDefaultsRow`。

- [ ] **Step 4: 编译**

```bash
.\gradlew :composeApp:compileKotlinJvm :composeApp:compileDebugKotlinAndroid --no-daemon
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add -A composeApp/
git commit -m "refactor(ui): remove TranslateFileTab and settings merge strategy selector"
```

---

## Task 4: 项目模型与持久化

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/planner/TranslationWorkflowMode.kt`（已建则跳过）
- Modify: `ui/screens/main/MainDashboardModels.kt`
- Modify: `persistence/TranslationProjectIndex.kt`
- Modify: `persistence/TranslationProjectFileStore.kt`
- Modify: `ui/screens/main/TranslationProjectRepository.kt`
- Modify: `commonTest/.../RecentXmlProjectRepositoryTest.kt`

- [ ] **Step 1: 扩展 `RecentXmlProject`**

```kotlin
data class RecentXmlProject(
    // ...existing fields...
    val workflowMode: TranslationWorkflowMode = TranslationWorkflowMode.FULL,
    val targetDisplayName: String? = null,
    val hasTargetBaseline: Boolean = false,
    val targetBaselinePath: String = "",
)
```

- [ ] **Step 2: 扩展 `TranslationProjectIndexEntry`（ kotlinx.serialization，`ignoreUnknownKeys` 已有）**

```kotlin
val workflowMode: String = "FULL", // "INCREMENTAL" | "FULL"
val targetDisplayName: String? = null,
val hasTargetBaseline: Boolean = false,
val targetBaselinePath: String = "",
```

`toRecentXmlProject()` / `toIndexEntry()` 映射 `TranslationWorkflowMode`。

- [ ] **Step 3: `TranslationProjectFileStore` 增加常量与方法**

```kotlin
private const val TARGET_BASELINE_FILE = "target-baseline.xml"

fun targetBaselinePath(projectId: String): String =
    "${projectDirectory(projectId)}/$TARGET_BASELINE_FILE"

fun writeTargetBaseline(projectId: String, xml: String) {
    writeTextFileAtomic(targetBaselinePath(projectId), xml)
}

fun readTargetBaseline(project: RecentXmlProject): String =
    if (project.targetBaselinePath.isNotBlank() && fileExists(project.targetBaselinePath)) {
        readTextFile(project.targetBaselinePath)
    } else ""

fun createIncrementalProject(
    sourceXml: String,
    targetXml: String,
    sourceDisplayName: String,
    targetDisplayName: String,
    sourceLang: String,
    targetLang: String,
): RecentXmlProject {
    val id = "${sourceDisplayName}_${Random.nextInt(1_000_000)}"
    ensureDirectory(projectDirectory(id))
    writeTextFileAtomic(sourcePath(id), sourceXml)
    val targetPath = targetBaselinePath(id)
    writeTextFileAtomic(targetPath, targetXml)
    writeTextFileAtomic(resultPath(id), targetXml) // 初始 result = baseline
    val total = IncrementalTranslationPlanner.plan(
        StringsXmlCodec.parse(sourceXml),
        StringsXmlCodec.parse(targetXml),
        forceTranslation = false,
    ).count { it.action == PlannedEntryAction.TRANSLATE }.coerceAtLeast(1)
    return RecentXmlProject(
        id = id,
        displayName = sourceDisplayName,
        workflowMode = TranslationWorkflowMode.INCREMENTAL,
        targetDisplayName = targetDisplayName,
        hasTargetBaseline = true,
        targetBaselinePath = targetPath,
        totalKeys = total,
        // ...
    )
}

fun createFullProject(
    sourceXml: String,
    displayName: String,
    sourceLang: String,
    targetLang: String,
): RecentXmlProject {
    val project = createProjectFromUpload(...) // 复用现有写 source
    return project.copy(
        workflowMode = TranslationWorkflowMode.FULL,
        hasTargetBaseline = false,
        targetDisplayName = null,
    )
}
```

- [ ] **Step 4: Repository 新 API**

```kotlin
fun addIncrementalFromUpload(
    sourceXml: String,
    targetXml: String,
    sourceDisplayName: String,
    targetDisplayName: String,
    sourceLang: String,
    targetLang: String,
): RecentXmlProject

fun addFullFromUpload(
    sourceXml: String,
    displayName: String,
    sourceLang: String,
    targetLang: String,
): RecentXmlProject

fun readTargetBaseline(project: RecentXmlProject): String
```

`PersistentTranslationProjectRepository` 委托 `TranslationProjectFileStore`；`InMemory` 测试实现同步扩展。

- [ ] **Step 5: 旧 index 迁移**

`toRecentXmlProject()`：`workflowMode` 缺省 → `FULL`，`hasTargetBaseline = targetBaselinePath.isNotBlank()`。

- [ ] **Step 6: 测试 + Commit**

```bash
.\gradlew :composeApp:jvmTest --tests "fun.abbas.android_res_translator.ui.screens.main.*" --no-daemon
git commit -m "feat(persistence): workflow mode and target-baseline.xml for projects"
```

---

## Task 5: `EntryStatus.Skipped` 与 FileEditor 三态加载

**Files:**
- Modify: `FileEditorModels.kt`
- Modify: `FileEditorController.kt`
- Modify: `FileEditorControllerStore.kt`
- Modify: `FileEditorProgressSection.kt`
- Modify: `XmlEntryRow.kt`（若有状态展示）
- Create/Modify: `FileEditorControllerTest.kt`

- [ ] **Step 1: 扩展 `FileEditorState`**

```kotlin
sealed interface EntryStatus {
    data object Pending : EntryStatus
    data object Translating : EntryStatus
    data object Completed : EntryStatus
    data object Skipped : EntryStatus  // 增量：保留已有译文
    data class Error(val message: String) : EntryStatus
}

val skippedCount: Int
    get() = translatableEntries.count { it.status is EntryStatus.Skipped }
```

`isExportReady` / `progressPercent`：`totalCount` 含 Pending+Completed+Skipped+Error；已完成 = Completed only。

- [ ] **Step 2: `FileEditorController` 构造参数**

```kotlin
class FileEditorController(
    // ...
    private val workflowMode: TranslationWorkflowMode,
    private var targetBaselineXml: String?,
    private val forceTranslation: Boolean,
    private val onTargetBaselinePersist: ((String) -> Unit)? = null,
)
```

- [ ] **Step 3: `loadIncremental`**

```kotlin
fun loadIncremental(sourceXml: String, targetXml: String) {
    parsedFile = StringsXmlCodec.parse(sourceXml)
    val targetModel = StringsXmlCodec.parse(targetXml)
    val planned = IncrementalTranslationPlanner.plan(parsedFile, targetModel, forceTranslation)
    _state.update {
        it.copy(
            entries = planned.toXmlEntryUiList(),
            isRunning = false,
            exportMessage = null,
        )
    }
}
```

- [ ] **Step 4: `attachTargetForFullMode`**

```kotlin
fun attachTargetForFullMode(targetXml: String) {
    targetBaselineXml = targetXml
    onTargetBaselinePersist?.invoke(targetXml)
    val targetModel = StringsXmlCodec.parse(targetXml)
    val planned = /* AllReplace planner: all translatable strings → Pending */
    _state.update { it.copy(entries = planned) }
}
```

全量 Planner 可简化为：`source.strings` 中 `translatable` → 全部 `Pending`（不生成 Skipped）。

- [ ] **Step 5: `canStartTranslation` / `canExport`**

```kotlin
fun canStartTranslation(): Boolean =
    when (workflowMode) {
        TranslationWorkflowMode.INCREMENTAL -> targetBaselineXml != null
        TranslationWorkflowMode.FULL -> !targetBaselineXml.isNullOrBlank()
    } && _state.value.pendingCount > 0
```

翻译循环：`Pending` 与 `Error` 可重试；**不**处理 `Skipped`。

- [ ] **Step 6: `FileEditorScreen` — 全量无 target Banner**

```kotlin
if (workflowMode == TranslationWorkflowMode.FULL && !hasTargetBaseline) {
    FullModeTargetRequiredBanner(
        onUploadTarget = { xmlFileAccess.launchPickXml { ... controller.attachTargetForFullMode(it) } },
    )
}
```

开始翻译按钮：`enabled = controller.canStartTranslation() && !state.isRunning`。

- [ ] **Step 7: `FileEditorControllerStore.getOrCreate` 传参**

从 `RecentXmlProject` 读取 `workflowMode`、`readTargetBaseline`；`FULL` 且无 baseline 时不调用 `load(source)`。

- [ ] **Step 8: 测试**

```kotlin
@Test fun fullMode_withoutTarget_cannotTranslate() { /* pendingCount 0 or canStart false */ }
@Test fun incremental_marksSkipped() { /* 1 filled target, 1 missing → skippedCount=1 */ }
```

- [ ] **Step 9: Commit**

```bash
git commit -m "feat(editor): incremental/full load paths, Skipped status, full-mode target gate"
```

---

## Task 6: Dashboard 双上传区

**Files:**
- Modify: `FileProjectsSection.kt`
- Modify: `MainDashboardScreen.kt`
- Modify: `UploadXmlDrop.jvm.kt`（增量区支持拖入 2 个文件，可选本任务或 Task 7）
- Modify: `values/strings.xml`, `values-zh/strings.xml`

- [ ] **Step 1: i18n 键**

```xml
<string name="dashboard_upload_xml_incremental">Upload XML (Incremental)</string>
<string name="dashboard_upload_hint_incremental">Source + target: translate missing or empty keys only</string>
<string name="dashboard_upload_xml_full">Upload XML (Full Replace)</string>
<string name="dashboard_upload_hint_full">Source only; upload target in editor to overwrite</string>
<string name="dashboard_incremental_pick_target">Select target strings.xml</string>
<string name="dashboard_incremental_summary">Translate %1$d · Skip %2$d</string>
```

- [ ] **Step 2: `FileProjectsSection` 网格 Cell 类型**

```kotlin
private sealed interface Cell {
    data class Project(val project: RecentXmlProject) : Cell
    data object UploadIncremental : Cell
    data object UploadFull : Cell
}
val cells = projects.map { Cell.Project(it) } + Cell.UploadIncremental + Cell.UploadFull
```

两个 `UploadXmlCard` 变体：`title` / `hint` / `onClick` / `onDrop` 不同。

- [ ] **Step 3: `MainDashboardScreen` 增量创建状态机**

```kotlin
var pendingIncrementalSource by remember { mutableStateOf<DroppedXmlFile?>(null) }

fun onIncrementalDrop(files: List<DroppedXmlFile>) {
    when {
        files.size >= 2 -> createIncremental(files[0], files[1])
        files.size == 1 && pendingIncrementalSource == null -> pendingIncrementalSource = files[0]
        files.size == 1 && pendingIncrementalSource != null -> createIncremental(pendingIncrementalSource!!, files[0])
    }
}

fun createIncremental(source: DroppedXmlFile, target: DroppedXmlFile) {
    val project = projectRepository.addIncrementalFromUpload(...)
    pendingIncrementalSource = null
    mode = DashboardUiMode.Editor(project.id)
}
```

点击增量区：若已有 `pendingIncrementalSource`，第二次 `launchPickXml` 作为目标；否则第一次为源。

- [ ] **Step 4: 全量创建**

```kotlin
fun onFullUpload(xml: String, name: String) {
    val project = projectRepository.addFullFromUpload(xml, name, snap.defaultSourceLang, snap.defaultTargetLang)
    mode = DashboardUiMode.Editor(project.id)
}
```

- [ ] **Step 5: 进入 Editor 时传入 baseline**

`MainDashboardScreen` / `FilesScreen`：`readTargetBaseline(project)` 传入 `FileEditorControllerStore`。

- [ ] **Step 6: 手动验证 + Commit**

```bash
.\gradlew :composeApp:run
# 上区拖 2 个 xml → 增量项目；下区拖 1 个 → 全量项目 → 详情页上传目标
git commit -m "feat(dashboard): dual upload zones for incremental and full workflows"
```

---

## Task 7: JVM 双文件拖放（增量上传区）

**Files:**
- Modify: `composeApp/src/jvmMain/kotlin/fun/abbas/android_res_translator/ui/files/UploadXmlDrop.jvm.kt`
- Modify: `commonMain/.../UploadXmlDrop.kt` 文档

- [ ] **Step 1: `onDrop` 支持多文件**

已有 `readDroppedXmlFilesSync` 返回 `List<DroppedXmlFile>`；Dashboard `onIncrementalDrop` 已处理 1/2 文件逻辑。

- [ ] **Step 2: 仅增量 Upload 卡启用 drop；全量卡单文件**

全量区 `onDrop`：`files.take(1)` 调用 `onFullUpload`。

- [ ] **Step 3: Commit（若与 Task 6 分开）**

```bash
git commit -m "feat(desktop): incremental upload zone accepts two XML files via drag-drop"
```

---

## Task 8: Files Tab 对齐 + 文档

**Files:**
- Modify: `FileBrowserScreen.kt` / `FilesScreen.kt`
- Modify: `README.zh-CN.md`
- Modify: `TRANSLATION_VENDOR.md`（若提及 consumerMode）

- [ ] **Step 1: Files FAB 上传**

打开 XML 走 **增量** 链路：选源后 `launchPickXml` 再选目标（与 Dashboard 上区一致），或导航到 Dashboard。

- [ ] **Step 2: 更新 README 功能特性**

说明：增量需源+目标；全量需详情页补目标；设置页不再选择合并策略。

- [ ] **Step 3: 全量编译与测试**

```bash
.\gradlew :composeApp:compileKotlinJvm :composeApp:jvmTest --no-daemon
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git commit -m "docs: document incremental vs full translation workflows"
```

---

## Spec 覆盖自检

| Spec 要求 | Task |
|-----------|------|
| string 空值增量 | Task 1 |
| array 按 item 增量 | Task 1 |
| 删除 TranslateFileTab | Task 3 |
| 删除设置合并策略 | Task 3 |
| workflowMode 绑定 Filled/AllReplace | Task 4–5 |
| target-baseline 持久化 | Task 4 |
| Dashboard 双上传区 | Task 6–7 |
| 全量详情页上传目标 | Task 5 |
| EntryStatus.Skipped | Task 5 |
| 旧项目 → FULL | Task 4 |
| Files Tab | Task 8 |

---

## 执行方式

Plan 已保存至 `docs/superpowers/plans/2026-05-21-incremental-translation-implementation.md`。

**两种执行方式：**

1. **Subagent-Driven（推荐）** — 按 Task 1→8 派发子代理，每 Task 完成后审查  
2. **Inline Execution** — 本会话使用 executing-plans 按检查点批量实现  

你希望用哪种方式开始实现？
