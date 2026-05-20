# UI 国际化（i18n）迁移 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 按任务顺序执行。步骤使用 `- [ ]` 勾选跟踪。

**Goal:** 将 `commonMain` Compose UI 硬编码文案迁移至 **composeResources** 多语言 `strings.xml`，并支持 Settings 切换界面语言（en / zh）。

**Architecture:** `AppSettingsSnapshot` 持久化 `uiLocale`；根节点 `CompositionLocal` + CMP `stringResource`；默认 `values/` 英文、`values-zh/` 简体中文。业务翻译语言与界面语言分离。错误文案经 `TranslationFailureUi` 门面按 locale 解析。

**Tech Stack:** Compose Multiplatform 1.10、`compose.components.resources`、`AppSettingsRepository`、既有 `AppTheme` / `AppRoot`。

**设计输入:** [2026-05-20-ui-i18n-migration.md](../specs/2026-05-20-ui-i18n-migration.md)、[.cursorrules](../../../.cursorrules)

---

## 文件结构（计划新增/修改）

| 路径 | 职责 |
|------|------|
| `composeApp/src/commonMain/composeResources/values/strings.xml` | 默认英文 UI 字符串 |
| `composeApp/src/commonMain/composeResources/values-zh/strings.xml` | 简体中文 |
| `composeApp/build.gradle.kts` | `compose.resources { }`（如需） |
| `ui/i18n/AppLocale.kt` | `en` / `zh` 枚举与 BCP-47 映射 |
| `ui/i18n/AppLocaleProvider.kt` | `CompositionLocal<AppLocale>`、`ProvideAppLocale` |
| `ui/i18n/AppStrings.kt` | 非 Composable 取串（错误信息等） |
| `ui/settings/AppSettingsModels.kt` | `uiLocale` 字段与持久化键 |
| `ui/settings/SettingsComponents.kt` | 界面语言选择 UI |
| `ui/AppRoot.kt` | 注入 locale、包 `ProvideAppLocale` |
| `ui/TranslationFailureUi.kt` | 按 locale 返回本地化错误 |
| `ui/**` 各屏幕 | `Text("...")` → `stringResource` |

---

### Task 1: Gradle 与资源骨架

**Files:**
- Modify: `composeApp/build.gradle.kts`
- Create: `composeApp/src/commonMain/composeResources/values/strings.xml`
- Create: `composeApp/src/commonMain/composeResources/values-zh/strings.xml`

- [ ] **Step 1: 确认/添加 compose resources 配置**

在 `composeApp/build.gradle.kts` 的 `kotlin { }` 或插件块中（按当前 CMP 文档）添加：

```kotlin
compose.resources {
    publicResClass = true
}
```

- [ ] **Step 2: 创建最小 `values/strings.xml`**

```xml
<resources>
    <string name="app_name">KMP Translator</string>
    <string name="common_cancel">Cancel</string>
    <string name="common_confirm">Confirm</string>
    <string name="common_close">Close</string>
</resources>
```

- [ ] **Step 3: 创建 `values-zh/strings.xml` 对应翻译**

```xml
<resources>
    <string name="app_name">KMP 翻译器</string>
    <string name="common_cancel">取消</string>
    <string name="common_confirm">确认</string>
    <string name="common_close">关闭</string>
</resources>
```

- [ ] **Step 4: 编译验证**

```bat
.\gradlew.bat :composeApp:compileKotlinJvm
```

确认生成 `Res.string.common_cancel` 等（在 `build/generated/...` 或 IDE 索引中可见）。

- [ ] **Step 5: Commit**

```bash
git add composeApp/build.gradle.kts composeApp/src/commonMain/composeResources/
git commit -m "chore(i18n): add composeResources strings skeleton (en/zh)"
```

---

### Task 2: 界面语言设置与 CompositionLocal

**Files:**
- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/ui/i18n/AppLocale.kt`
- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/ui/i18n/AppLocaleProvider.kt`
- Modify: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/ui/settings/AppSettingsModels.kt`
- Modify: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/ui/settings/AppSettingsRepository.kt`（及 jvm/android/ios actual 若需）
- Modify: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/ui/AppRoot.kt`

- [ ] **Step 1: 定义 `AppLocale`**

```kotlin
enum class AppLocale(val tag: String) {
    En("en"),
    Zh("zh"),
    ;

    companion object {
        fun fromTag(tag: String?): AppLocale =
            entries.firstOrNull { it.tag.equals(tag?.trim(), ignoreCase = true) } ?: En
    }
}
```

- [ ] **Step 2: `AppSettingsSnapshot` 增加 `uiLocale`**

- 字段：`val uiLocale: AppLocale = AppLocale.En`
- 持久化键：`const val KEY_UI_LOCALE = "ui.uiLocale"`
- 在 `fromFlatMap` / `toFlatMap`（或等价序列化）中读写 `tag`

- [ ] **Step 3: `CompositionLocal` 与 Provider**

```kotlin
val LocalAppLocale = compositionLocalOf { AppLocale.En }

@Composable
fun ProvideAppLocale(
    locale: AppLocale,
    content: @Composable () -> Unit,
) {
    // 按 CMP 1.10 文档设置 ResourceEnvironment / Locale
    // 并 CompositionLocalProvider(LocalAppLocale provides locale)
    content()
}
```

**实现注意：** 查阅 [Compose Multiplatform Resources](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-images-resources.html) 中 **language qualifiers** 与运行时 locale；若 API 为 `rememberResourceEnvironment { }`，封装在 `ProvideAppLocale` 内，避免各屏幕重复。

- [ ] **Step 4: `AppRoot` 接线**

```kotlin
val snap by settings.snapshot.collectAsState()
ProvideAppLocale(locale = snap.uiLocale) {
    AppTheme(appearance = snap.appAppearance) { /* 现有内容 */ }
}
```

- [ ] **Step 5: Settings 增加「界面语言」**

在 `SettingsScreen` / `SettingsComponents` 增加 Segmented 或下拉：`English` / `简体中文`，写入 `settings.updateUiLocale(AppLocale.Zh)`。

- [ ] **Step 6: 手工验证**

运行 `:composeApp:run`，切换语言后 Dashboard 标题等是否变化（需 Task 3 后更明显；此步可先验证 Provider 不崩溃）。

- [ ] **Step 7: Commit**

```bash
git commit -m "feat(i18n): add uiLocale setting and CompositionLocal provider"
```

---

### Task 3: 公共字符串与 Dashboard 模块迁移

**Files:**
- Modify: `composeResources/values/strings.xml`, `values-zh/strings.xml`
- Modify: `ui/screens/main/FileProjectsSection.kt`
- Modify: `ui/screens/main/QuickTranslateSection.kt`
- Modify: `ui/screens/main/EnginePickerDialog.kt`
- Modify: `ui/screens/main/LanguagePickerDialog.kt`
- Modify: `ui/components/FileProjectCard.kt`（`formatModifiedAgo`）

- [ ] **Step 1: 提取 Dashboard 相关 key 写入 XML**

示例 key（英文 → 中文在 values-zh）：

| key | en |
|-----|-----|
| `dashboard_file_projects_title` | File Projects |
| `dashboard_view_all` | VIEW ALL |
| `dashboard_upload_xml` | Upload XML |
| `dashboard_upload_hint` | Drop your strings.xml here |
| `file_projects_delete_title` | Delete project |
| `file_projects_delete_message` | Delete "%1$s"? Local translation data will be removed and cannot be recovered. |
| `quick_translate_title` | Quick Translate |
| `engine_picker_title` | Select translation engine |

- [ ] **Step 2: 替换 `FileProjectsSection` 对话框与标题**

```kotlin
import org.jetbrains.compose.resources.stringResource
import ...generated.resources.Res

Text(stringResource(Res.string.dashboard_file_projects_title))
```

删除确认使用 `%1$s` 传入 `project.displayName`。

- [ ] **Step 3: 替换 `QuickTranslateSection`、`EnginePickerDialog`、`LanguagePickerDialog`**

- [ ] **Step 4: `FileProjectCard.formatModifiedAgo`**

改为接收 `@Composable` 上下文下的 `stringResource`，或把 4 条时间文案迁入资源。

- [ ] **Step 5: 编译 + run 抽查 en/zh**

- [ ] **Step 6: Commit**

```bash
git commit -m "feat(i18n): migrate dashboard and file project strings"
```

---

### Task 4: 文件编辑器模块迁移

**Files:**
- Modify: `composeResources/values/strings.xml`, `values-zh/strings.xml`
- Modify: `ui/screens/fileeditor/FileEditorScreen.kt`
- Modify: `ui/screens/fileeditor/FileEditorProgressSection.kt`
- Modify: `ui/screens/fileeditor/FileEditorActionsCard.kt`
- Modify: `ui/screens/fileeditor/XmlEntryRow.kt`（若有硬编码）

- [ ] **Step 1: 添加 file_editor_* 资源键**

覆盖：Overall Progress、Total/Translated/Pending/Error、Start/Pause Translation、Export、重新翻译/导出确认对话框、Filter keys、无匹配条目、Snackbar（导出/未完成）。

参考现有中文文案写入 `values-zh`（与当前 UI 一致）。

- [ ] **Step 2: 迁移 `FileEditorProgressSection` / `FileEditorActionsCard`**

- [ ] **Step 3: 迁移 `FileEditorScreen` 对话框与 Snackbar**

```kotlin
snackbarHostState.showSnackbar(stringResource(Res.string.file_editor_export_not_ready))
```

- [ ] **Step 4: 回归测试**

- 开始翻译、暂停、导出确认、有 Error 时导出、重新翻译对话框
- 切换 zh 后按钮与对话框为中文

- [ ] **Step 5: Commit**

```bash
git commit -m "feat(i18n): migrate file editor UI strings"
```

---

### Task 5: 文件浏览、设置、根导航

**Files:**
- Modify: `ui/AppRoot.kt`, `ui/screens/FilesScreen.kt`, `ui/screens/files/*`, `ui/screens/settings/*`, `ui/screens/TranslateScreens.kt`

- [ ] **Step 1: 盘点并添加 `files_*`、`settings_*`、`app_root_*` keys**

- [ ] **Step 2: 逐文件替换 `stringResource`**

- [ ] **Step 3: `contentDescription` 可访问性字符串迁入 `strings.xml`（如 `settings`）**

- [ ] **Step 4: Commit**

```bash
git commit -m "feat(i18n): migrate files, settings, and app root strings"
```

---

### Task 6: 翻译错误与非 Composable 文案

**Files:**
- Create: `ui/i18n/AppStrings.kt`
- Modify: `ui/TranslationFailureUi.kt`
- Modify: `composeResources/values/strings.xml`, `values-zh/strings.xml`

- [ ] **Step 1: 错误信息资源键**

| key | 用途 |
|-----|------|
| `error_no_vendor_for_target` | No vendor for %1$s |
| `error_network` | Network error: %1$s |
| `error_lingvanex_payment` | Lingvanex billing hint |
| `error_vendor_missing_key` | Missing API key |
| … | 对应 `vendorRejectedUserMessage` 分支 |

- [ ] **Step 2: `AppStrings` 门面（非 Composable）**

```kotlin
object AppStrings {
    fun translationFailure(message: TranslationFailure, locale: AppLocale): String {
        // 使用 Generated 资源的 getString API（按 CMP 文档）
        // 或注入 ResourceReader
    }
}
```

查阅 CMP 是否提供 `getString(resource, Environment)`；若无，在 `ViewModel`/调用处改为 Composable 层展示错误，或缓存当前 locale 的解析结果。

- [ ] **Step 3: 更新 `FileEditorController` / UI 调用链**

`outcome.failure.toUserMessage()` → `AppStrings.translationFailure(..., LocalAppLocale.current)` 或在 Composable 内直接 `stringResource` 映射。

- [ ] **Step 4: Commit**

```bash
git commit -m "feat(i18n): localize translation error messages"
```

---

### Task 7: About 页与收尾（可选）

**Files:**
- Modify: `ui/screens/about/AboutSections.kt`
- Modify: `README.md`（简短说明界面语言）

- [x] **Step 1: 决策** — About 标题/正文/按钮迁入 `values` + `values-zh`；许可库名称与描述保留英文（`AboutContent` 数据）

- [x] **Step 2: 执行迁移或登记技术债**

- [x] **Step 3: 全库扫描残留**

```bash
# 在 composeApp/src/commonMain/kotlin/.../ui 下搜索
Text\("
title = \{ Text\(
showSnackbar\("
```

- [x] **Step 4: 更新 README「使用说明」** — 增加 Settings → UI language

- [ ] **Step 5: Commit**（按需由维护者执行）

```bash
git commit -m "docs(i18n): complete about page and document UI locale in README"
```

---

## 实施顺序与估时

| 任务 | 预估 | 依赖 |
|------|------|------|
| Task 1 | 30 min | — |
| Task 2 | 1–2 h | Task 1 |
| Task 3 | 2 h | Task 2 |
| Task 4 | 2 h | Task 2 |
| Task 5 | 2 h | Task 2 |
| Task 6 | 1.5 h | Task 2 |
| Task 7 | 1 h | 可选 |

**建议迭代：** Task 1 → 2 → 3 → 4（用户最常路径）→ 5 → 6 → 7。

---

## 验证清单（发布前）

- [ ] `.\gradlew.bat :composeApp:jvmTest` 通过
- [ ] Desktop：Settings 切换 en ↔ zh，Dashboard + 文件编辑 + 删除项目对话框文案正确
- [ ] Android：同上（若方便）
- [ ] 翻译失败时错误提示随界面语言变化（非固定中文）
- [ ] 无新增 `config.properties` 或密钥提交

---

## 参考链接

- [Compose Multiplatform — Resources](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-images-resources.html)
- 项目 [TRANSLATION_VENDOR.md](../../../TRANSLATION_VENDOR.md)（业务翻译，非 UI i18n）
- 仓库 [.cursorrules](../../../.cursorrules)

---

## 执行方式说明

实施本计划时，在 **新会话** 中打开：

```text
docs/superpowers/plans/2026-05-20-ui-i18n-migration.md
```

并声明使用 **executing-plans** 或 **subagent-driven-development** 技能，按 Task 1→7 顺序执行，每 Task 完成后勾选 checkbox 并单独 commit。
