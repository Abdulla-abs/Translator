# feature.md 功能可行性分析与实现文档

**日期**：2026-05-25  
**依据**：[feature.md](../../feature.md)、现有 KMP 工程 `composeApp`、遗留核心 `android_trans`  
**设计约束**：[.cursorrules](../../.cursorrules)（UI 仅 `commonMain`、禁止 `android.widget.*`、优先 Compose Multiplatform）

---

## 1. 执行摘要

| # | 功能 | 可行性 | 建议阶段 | 预估工作量 |
|---|------|--------|----------|------------|
| 1 | 主页快速翻译 · 自动翻译（0.5s 防抖 + 取消） | **高** | P0 | 2–3 人日 |
| 2 | 翻译项目详情 · 可折叠设置区（迁移「强制翻译」） | **高** | P0 | 1–2 人日 |
| 3 | 设置页 · 默认 source/target 改为合并语言弹窗 | **高** | P0 | 2–3 人日 |
| 4 | 单文件项目详情 · 导出 Excel（`.xlsx`） | **中高** | P1 | 4–6 人日 |
| 5 | 双 string.xml 比对（自定义表格 + 吸顶） | **中** | P2 | 6–10 人日 |
| 6 | 多文件项目（高级） | **中高** | 独立子里程碑 | 见 [多文件项目子里程碑](./2026-05-25-res-multi-project-milestones.md)（§0 复评：P1/P2 已就绪） |

**总体结论**：在现有架构上均可实现；主路线图风险主要集中在 **(a) 双轴吸顶表格在 Compose 上的交互与性能**、**(b) 比对/导出对 `plurals` 的编解码扩展**（当前 `StringsXmlCodec` 尚未支持）。导出格式已确认为 **`.xlsx`**。多文件项目已拆至 **M1–M5** 子里程碑。建议按 P0 → P1 → P2 → 子里程碑顺序交付。

---

## 2. 现状基线（与 feature 的映射）

### 2.1 已有能力

| 领域 | 现有实现 | 路径 |
|------|----------|------|
| 快速翻译（手动按钮） | `performTranslate()` + `TranslationServices.translatePlainText` | `QuickTranslateSection.kt` |
| 语言选择弹窗 | `LanguagePickerDialog` + `LanguagePickerCatalog`（按**当前选中引擎**过滤） | `screens/main/LanguagePickerDialog.kt`、`translation/LanguagePickerCatalog.kt` |
| 强制翻译（待迁移） | 当前为全局 `AppSettingsSnapshot.forceTranslation` → `FileEditorController`；目标改为**按项目**持久化 | `FileEditorController.kt`、`EditorSessionPersistence.kt` |
| 单文件翻译项目 | 上传 XML、增量/全量、`isExportReady`、导出 XML | `FileEditorScreen.kt`、`TranslationProjectFileStore.kt` |
| values 目录扫描与写回 | `TranslateValuesFolderUseCase`、`ValuesFolderCountryTransformer` | `core/resources/usecase/` |
| strings.xml 解析 | `StringsXmlCodec`（xmlutil，KMP） | `core/resources/xml/StringsXmlCodec.kt` |
| 遗留 XLS | jxl 读写 `.xls`，**仅 JVM** | `android_trans/.../StringXmlOutput.java`、`Jxl2Dom4j.java` |

### 2.2 明确缺口

- 设置项：`quickTranslateAutoMode`（自动/手动翻译）不存在。
- 项目级 `forceTranslation` 未实现：需迁入项目详情设置并写入会话/项目元数据（**默认 false**，互不影响）。
- 设置页默认语言为 **自由文本** `OutlinedTextField`，非合并引擎语言弹窗。
- 无 `.xlsx` **导入**、多文件项目见 [子里程碑文档](./2026-05-25-res-multi-project-milestones.md)（**导出**与双文件比对 UI 已随 P1/P2 完成）。
- `StringsXmlCodec` 已支持 `plurals`（P2）；多文件导出矩阵须基于 `ResourceFlattener` 新建 builder，勿沿用仅两列的 `StringsMatrixBuilder.buildForFileEditor`。
- `Files` Tab 仅有「文件项目」一条线，无「文件比对 / 多文件项目」子模块路由。
- 快速翻译成功路径仍会调用 `copyResultWithSnackbar`（待 P0 移除，与复制按钮解耦）。

---

## 3. 分功能可行性分析

### 3.1 主页快速翻译 · 自动翻译

**需求**：输入源文后 0.5s 无变更则自动翻译；翻译中改文则取消当前请求并重新计时；设置可切回手动；自动模式下隐藏翻译按钮。

**可行性：高**

| 维度 | 说明 |
|------|------|
| 技术栈 | 工程已使用 `rememberCoroutineScope`、`launch`；与需求中的协程、取消、背压一致。 |
| 实现模式 | `snapshotFlow { input }` + `debounce(500)` + `flatMapLatest`：新输入自动取消上一次 `translatePlainText`。 |
| 背压 | 以「只保留最新一次有效翻译」为准即可（`flatMapLatest` / 递增 `requestId` 丢弃过期结果）。 |
| UI | `AppSettingsSnapshot` 增加 `quickTranslateMode: Auto \| Manual`；`QuickTranslateTargetColumn` 按模式 `AnimatedVisibility` 按钮。 |
| 边界 | 空输入、仅空白、引擎未配置、loading 中重复触发需 guard；自动/手动模式均保留**复制按钮**（独立操作）。 |
| 剪贴板 | **已确认**：自动翻译与手动点击翻译按钮，成功后**均不**写入剪贴板；复制仅由用户点击复制按钮触发。实现时需移除 `performTranslate` 成功分支中的 `copyResultWithSnackbar`（与复制按钮无关）。 |

**依赖**：无新三方库。

---

### 3.2 翻译项目详情 · 可折叠设置区（强制翻译迁移）

**需求**：详情页顶部可展开/收起的设置区；将设置页的「强制翻译」迁入此处。

**可行性：高**

| 维度 | 说明 |
|------|------|
| 数据 | `forceTranslation` 已在 `FileEditorController` 构造参数中使用（`AbsTranslationConsumer` 语义已 KMP 化）。 |
| UI | 在 `FileEditorScreen` 增加 `ExpandableSettingsPanel`（`AnimatedVisibility` + 点击标题切换）。 |
| 设置页 | 从 `SettingsStrategiesCard` 移除 `StrategyToggleRow`（强制翻译），避免双入口。 |
| 持久化 | **已确认：按项目持久化**。写入 `FileEditorSessionSnapshot`（或 `TranslationProjectIndexEntry` 扩展字段）；**每个项目默认 `forceTranslation = false`**；用户在某项目详情内开启仅影响该项目，不读写全局 `AppSettingsSnapshot.forceTranslation`（该字段废弃或迁移后删除）。 |
| 加载 | 打开项目时从会话读取 `forceTranslation` 注入 `FileEditorController`；切换开关即时 `persist` 并作用于后续翻译计划。 |

---

### 3.3 设置页 · 默认 source/target 合并语言弹窗

**需求**：与主页一致的选择弹窗；选项 = **所有已配置引擎** 支持语言的**并集**；处理 `zh` / `zh-cn` 等同义不同写法。

**可行性：高**

| 维度 | 说明 |
|------|------|
| 引擎判定 | 复用 `TranslationEngineCatalog.engineOptions(snap)` 过滤已配置密钥的引擎。 |
| 合并算法 | 新建 `MergedLanguageCatalog`：`unionSourceCodes(snapshot)` / `unionTargetCodes(snapshot, currentSource)`，对各 `*LanguageSupport` 的 app code 做 `distinct` + **规范化**。 |
| 别名 | 引入 `LangCodeCanonicalizer`：`zh` ↔ `zh-cn` ↔ `zh-CHS` 等映射到 canonical（建议以 **Android values 文件夹习惯** `zh`、`zh-rTW` 为主键，见 `ValuesFolderCountryTransformer`）。 |
| UI | `LocalizationDefaultsRow` 将 `SettingsKeyField` 改为 `AppLanguageChip` + 点击打开 `LanguagePickerDialog`（`options` 来自 `MergedLanguageCatalog`，无单引擎标题时可显示「已配置引擎合并」）。 |
| 与快速翻译差异 | 主页按**当前选中引擎**过滤；设置页按**并集**——符合 feature 描述，需在 UI 文案区分。 |

**风险**：并集后某些 source/target 组合对特定引擎非法；设置页仅存「偏好默认值」，真正翻译时仍由当前引擎校验（已有 `supportsLanguagePair`）。

---

### 3.4 单文件项目详情 · 导出 Excel（`.xlsx`）

**需求**：翻译完成后可导出表格文件；未完成则按钮禁用。参考 `android_trans` 的 xml→表格逻辑；[feature.md](../../feature.md) 原文写作 `.xls`，**已确认对外使用 `.xlsx`**。

**可行性：中高**

#### 3.4.1 遗留逻辑（可移植部分）

`StringXmlOutput.outputAll` 核心步骤：

1. 扫描 `values*` 目录 → `Map<lang, strings.xml>`
2. 合并所有 key（含 `string` / `string-array` **按 item 展平**，见 legacy `name + index`）
3. 第一列 key，后续列各语言 value
4. 旧版 jxl 写入 `.xls`（**不采用**，仅作算法参考）

单文件项目场景可简化为：**源 XML + 目标 XML 两列**（或增量模式下 baseline + result），不必一次扫整个 res 目录。

#### 3.4.2 格式（已确认）

| 项 | 决策 |
|----|------|
| 对外格式 | **`.xlsx` only**（UI 文案：「导出 Excel」/「导出 .xlsx」） |
| 实现库 | [ooxml-kotlin](https://github.com/Nillerr/ooxml-kotlin) 或 [FileMapper-KMP](https://github.com/mamon-aburawi/FileMapper-KMP)（P1 POC 选定） |
| `.xls`（jxl） | **不做**（与 KMP/iOS 目标冲突） |

#### 3.4.3 行键与资源类型（与 §3.5 对齐）

导出矩阵行键（`CompareRowKey` / `StringsMatrixRow.key`）统一约定：

| 资源 | 行键示例 | 说明 |
|------|----------|------|
| `<string name="app_name">` | `app_name` | 与现有一致 |
| `<string-array name="items">` 第 j 项 | `items0`、`items1`… | 对齐 legacy `StringXmlOutput`（`name + itemIndex`） |
| `<plurals name="errors">` 某 quantity | `errors#one`、`errors#other`… | **需扩展** `StringsXmlCodec` + 模型后导出 |

P1 单文件导出至少覆盖 **string + string-array**；若 P1 完成前 codec 已支持 plurals，则一并导出。

#### 3.4.4 集成点

- `core/resources/export/StringsMatrixExporter` + `XlsxEncoder`：`StringsMatrix` → `ByteArray`（`.xlsx`）。
- `FileEditorActionsCard`：「导出 Excel」，`enabled = state.isExportReady`。
- `expect suspend fun saveSpreadsheet(bytes, suggestedName: String): Boolean`（建议默认扩展名 `.xlsx`）。

**风险**：`plurals` 编解码为 P1/P2 共享前置；展平后行数增多，需注意大表性能（与 P2 表格虚拟化一致）。

---

### 3.5 string.xml 文件比对

**需求**：主页文件模块下新增「文件比对」；两文件上传 → 表格展示 key × 语言列；差异行红底；列头与行头吸顶；Compose 自定义表格，Material 风格。

**可行性：中**（因需 **`plurals` 编解码扩展**，工作量高于仅 `string`）

| 维度 | 说明 |
|------|------|
| 资源范围 | **已确认**：须支持 `<string>`、`<string-array>`（按 item 展平为多行）、`<plurals>`（按 quantity 展平为多行）。行键规则与 §3.4.3 一致。 |
| 数据层 | `CompareMatrixBuilder`：两次解析 → 行键并集 → 按列填 cell；差异 `normalizeWs(a) != normalizeWs(b)`。`string-array` / `plurals` 缺项视为空串参与比对。 |
| Codec 前置 | 扩展 `StringResourceFile` + `StringsXmlCodec`：新增 `plurals` 模型（`name` + `quantity` → `value`）；解析时跳过或保留未知标签策略需与序列化 round-trip 一致。 |
| 路由 | 扩展 `Files`：子 Tab `文件项目 \| 文件比对`；`CompareProjectRepository` 持久化两文件路径与元数据。 |
| 表格 UI | 自研 `StickyCompareGrid` 或验证 CMP 表格库；左列展示行键（可对 `string-array`/`plurals` 行用次要样式区分类型）。 |
| 吸顶 | 列头 + 行键列双轴吸顶（见 §3.5 原技术选项）。 |
| 性能 | 展平后行数 = strings + Σ array items + Σ plural quantities；>2000 行须 `LazyColumn` 虚拟化。 |

**风险**：双轴吸顶 + 横向滚动同步；**plurals** 在 Android 中 quantity 集合因 locale 而异，比对时以**两侧文件内实际出现的 quantity 并集**为行集，缺失 quantity 按空值比对。

---

### 3.6 多文件项目（高级）

**不在本文展开。** 已拆分为独立子里程碑文档：

→ **[2026-05-25-res-multi-project-milestones.md](./2026-05-25-res-multi-project-milestones.md)**（M1 项目壳与初始化 → M2 详情信息 → M3 Excel 导出 → M4 导入比对覆写 → M5 版本追踪）

---

## 4. 推荐架构增量

```
composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/
├── core/
│   ├── resources/
│   │   ├── export/          # StringsMatrixExporter, XlsxEncoder (interface)
│   │   └── compare/         # CompareMatrixBuilder, DiffHighlighter
│   └── text/                # (existing)
├── persistence/
│   ├── CompareProjectFileStore.kt
│   └── (多文件项目 store 见子里程碑文档)
└── ui/
    ├── components/
    │   └── grid/            # StickyCompareGrid, CompareCell
    ├── screens/
    │   ├── compare/         # CompareList, CompareUpload, CompareDetail
    │   └── fileeditor/      # FileEditorSettingsPanel (expandable)
    └── translation/
        └── MergedLanguageCatalog.kt
```

**平台 actual（主路线图范围）**

- `SpreadsheetFileAccess`：保存/打开 xlsx（及可选 jvm xls）
- 目录选择、`ResMulti*` 模块见 [子里程碑文档](./2026-05-25-res-multi-project-milestones.md)

---

## 5. 实现阶段与任务分解

### P0 — 体验与设置（1 周）

| 任务 ID | 内容 | 验收标准 |
|---------|------|----------|
| P0-1 | `AppSettingsSnapshot.quickTranslateAuto` + 设置页开关 | 切换后主页按钮显示/隐藏；默认自动 |
| P0-2 | `QuickTranslateSection` debounce + 取消；移除翻译成功自动复制 | 0.5s 内连续输入只触发最后一次；改文取消 in-flight；自动/手动翻译均不写剪贴板 |
| P0-3 | `FileEditor` 可折叠设置 + 按项目 `forceTranslation` | 设置页无强制翻译项；每项目默认 false、独立持久化、互不影响 |
| P0-4 | `MergedLanguageCatalog` + 设置页语言弹窗 | 配置 huoshan+tencent 后选项为 zh∪en∪jp；zh/zh-cn 不重复出现 |
| P0-5 | 单测：`LangCodeCanonicalizer`、`debounce` 行为 | `commonTest` 通过 |

### P1 — 单项目 Excel 导出（1 周）

| 任务 ID | 内容 | 验收标准 |
|---------|------|----------|
| P1-1 | POC：选定 xlsx 库（Desktop + Android 各测一次写文件） | 记录依赖坐标与 ProGuard 规则 |
| P1-2 | `StringsMatrixExporter` + 双列（source/target）导出 `.xlsx` | 含 `string` + `string-array` 展平行；翻译未完成按钮 disabled；文件可在 Excel 打开 |
| P1-2b | （可选，与 P2 对齐）`StringsXmlCodec` 增加 `plurals` | round-trip 单测；导出/比对可复用同一展平器 |
| P1-3 | `saveSpreadsheet` expect/actual | 三端至少 JVM+Android 可保存；iOS 走文档目录 |

### P2 — 双文件比对（1.5–2 周）

| 任务 ID | 内容 | 验收标准 |
|---------|------|----------|
| P2-0 | `StringsXmlCodec` + `CompareMatrixBuilder` 支持 string / string-array / plurals | 行键展平单测；plurals quantity 并集行为 documented |
| P2-1 | `CompareProject` 持久化 + 列表 UI | 可创建项目、上传两个 xml |
| P2-2 | `StickyCompareGrid` MVP | 三类资源行均展示；差异红底；列头+行头吸顶；1000+ 行滚动流畅 |
| P2-3 | 导航与空态、错误 XML 提示 | 未上传两文件时比对按钮不可用 |

**P3（多文件项目）**：不列入主路线图任务表，按 [子里程碑 M1–M5](./2026-05-25-res-multi-project-milestones.md) 独立排期（建议主路线图 P1、P2 完成后再启动 M1）。

---

## 6. 关键 API 草图（实现参考）

### 6.1 自动翻译（P0）

```kotlin
// QuickTranslateSection.kt 概念
LaunchedEffect(input, snap.quickTranslateAuto) {
    if (!snap.quickTranslateAuto) return@LaunchedEffect
    snapshotFlow { input }
        .debounce(500)
        .collectLatest { text ->
            if (text.isBlank()) return@collectLatest
            performTranslate()
        }
}
```

### 6.2 合并语言（P0）

```kotlin
object MergedLanguageCatalog {
    fun allSourceOptions(snapshot: AppSettingsSnapshot): List<LanguagePickerOption>
    fun allTargetOptions(snapshot: AppSettingsSnapshot, source: String): List<LanguagePickerOption>
}

object LangCodeCanonicalizer {
    fun canonical(code: String): String  // e.g. zh-cn -> zh
    fun mergeDistinct(codes: Iterable<String>): List<String>
}
```

### 6.3 导出矩阵（P1；多文件全量合并见子里程碑 M3）

```kotlin
data class StringsMatrix(
    val columnHeaders: List<String>, // 首列空或 "key"，其余为语言码
    val rows: List<StringsMatrixRow>,
)
data class StringsMatrixRow(val key: String, val valuesByLang: Map<String, String>)

interface SpreadsheetEncoder {
    fun encode(matrix: StringsMatrix): ByteArray
}
```

### 6.4 资源展平与比对（P2）

```kotlin
/** 稳定行键：string → name；array → "${name}${index}"；plurals → "${name}#${quantity}" */
fun flattenToRows(file: StringResourceFile): List<FlatResourceRow>

fun buildCompareMatrix(
    left: StringResourceFile,
    right: StringResourceFile,
    leftLangLabel: String,
    rightLangLabel: String,
): CompareMatrix
```

---

## 7. 测试策略

| 层级 | 范围 |
|------|------|
| `commonTest` | `LangCodeCanonicalizer`、`flattenToRows`（string/array/plurals）、`StringsMatrixExporter`、`CompareMatrix` 差异、项目级 `forceTranslation` |
| `jvmTest` | 读写 xlsx 文件；多文件/版本测见子里程碑文档 |
| 手动 | Desktop 大表格滚动；Android SAF 选目录；iOS 文件权限 |

---

## 8. 非目标（主路线图阶段不做）

- Git 式分支、merge、冲突解决 UI
- 在线协作、云同步
- 翻译 API 本身的新厂商接入
- 多文件项目与版本追踪（见 [子里程碑](./2026-05-25-res-multi-project-milestones.md)）

---

## 9. 产品确认记录

**已确认（主路线图）**

| 项 | 决策 |
|----|------|
| 导出格式 | **`.xlsx` 可接受**；不实现 `.xls`（jxl）；UI 对外称 Excel / `.xlsx` |
| 双文件比对范围 | **需要**支持 `<string>`、`<string-array>`（按 item 展平）、`<plurals>`（按 quantity 展平） |
| 快速翻译剪贴板 | 自动/手动翻译成功后**均不**自动复制；仅复制按钮写入剪贴板 |
| 强制翻译 | **按项目**持久化，默认 false，项目间互不影响 |

**仍待确认（主路线图）**

- （无）

**多文件项目**：见 [子里程碑 §6](./2026-05-25-res-multi-project-milestones.md#6-产品确认记录)（与主路线图对齐项已同步）。

---

## 10. 参考文件索引

| 文件 | 用途 |
|------|------|
| [feature.md](../../feature.md) | 需求原文 |
| [QuickTranslateSection.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/ui/screens/main/QuickTranslateSection.kt) | 快速翻译现状 |
| [LanguagePickerCatalog.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/ui/translation/LanguagePickerCatalog.kt) | 单引擎语言表 |
| [SettingsComponents.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/ui/screens/settings/SettingsComponents.kt) | 强制翻译 + 默认语言输入框 |
| [StringXmlOutput.java](../../android_trans/src/main/java/org/example/dom4j/StringXmlOutput.java) | 多语言合并导出参考 |
| [Jxl2Dom4j.java](../../android_trans/src/main/java/org/example/dom4j/Jxl2Dom4j.java) | xls→xml 覆写参考 |
| [2026-05-14-android-trans-phase1-design.md](./2026-05-14-android-trans-phase1-design.md) | jxl 仅阶段 1.5 的既有决策 |
| [2026-05-19-file-page-design.md](./2026-05-19-file-page-design.md) | 单文件编辑器范围 |
| [2026-05-25-res-multi-project-milestones.md](./2026-05-25-res-multi-project-milestones.md) | 多文件项目 M1–M5 子里程碑 |

---

## 11. 修订记录

| 版本 | 日期 | 说明 |
|------|------|------|
| 0.1 | 2026-05-25 | 初稿：可行性 + 分阶段实现路径 |
| 0.2 | 2026-05-25 | 确认剪贴板/强制翻译策略；§3.6 拆至子里程碑文档 |
| 0.3 | 2026-05-25 | 确认 `.xlsx` 与比对含 string-array/plurals；补充 codec 与行键约定 |
