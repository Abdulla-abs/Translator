# UI 国际化（i18n）迁移 — 设计说明

**日期:** 2026-05-20  
**状态:** 待实施  
**关联计划:** [2026-05-20-ui-i18n-migration.md](../plans/2026-05-20-ui-i18n-migration.md)

---

## 背景

AndroidResTranslator（KMP Translator）的 Compose UI 位于 `composeApp/src/commonMain`，当前大量文案以 **Kotlin 字面量** 写在 `Text("...")`、对话框标题、Snackbar、`TranslationFailureUi` 等处。项目已依赖 `compose.components.resources`，`composeResources/` 下仅有 **drawable**，未使用字符串资源。

仓库 `.cursorrules` 要求共享 UI 使用 **composeResources**，不得用 Android 独有 API 承载跨平台文案。

---

## 目标

1. **共享 UI 文案** 迁入 `commonMain/composeResources` 的多语言 `strings.xml`。
2. 支持至少 **英文（默认）** 与 **简体中文**，与 [README.md](../../../README.md) / [README.zh-CN.md](../../../README.zh-CN.md) 语言策略一致。
3. 用户在 **Settings** 可选择 **界面语言**（与「翻译源/目标语言」分离）。
4. Desktop（JVM）、Android、iOS 三端行为一致；切换语言后无需重启即可生效（重组即可）。

---

## 非目标（明确排除）

| 范围 | 说明 |
|------|------|
| 用户工程的 `strings.xml` | 业务翻译对象，继续走 `StringsXmlCodec` / 文件编辑器，**不**并入 UI `composeResources` |
| 翻译厂商 API 语言码 | `LingvanexLanguageSupport` 等保持独立 |
| `formatLanguageLabel()` 短期可保留 | 语言**显示名**可二期迁入资源；首期不阻塞主流程 |
| About 页大段英文介绍文案 | 可维持现状或仅迁按钮/标题；完整本地化列为 Phase 3 可选 |
| RTL / 多地区变体 | 首期不做 `values-ar` 等；仅 `values` + `values-zh` |
| 自动从系统语言检测 | 二期可选；首期以设置项为准 |

---

## 技术选型

### 采用：Compose Multiplatform Resources（官方）

- 路径：`composeApp/src/commonMain/composeResources/`
- 限定符与 Android 类似：`values/`（默认 en）、`values-zh/`
- Composable 使用 `stringResource(Res.string.xxx)`
- 与现有 `compose-multiplatform.xml` drawable 共存

### 不采用：仅 `androidMain/res/values/strings.xml`

- 仅 Android 可读，`commonMain` UI 无法在 Desktop/iOS 使用。

### 不采用：Moko Resources（首期）

- 与官方 composeResources 能力重叠；避免双套资源体系。

---

## 架构

```text
AppSettingsSnapshot.uiLocale (持久化)
        │
        ▼
AppTheme / AppRoot 提供 CompositionLocal<AppLocale>
        │
        ▼
Compose Environment / Locale（CMP Resources API）
        │
        ▼
stringResource(Res.string.*)  →  values / values-zh
```

**原则：**

- **界面语言** `uiLocale`：`en` | `zh`（可扩展 `zh-TW`）
- **翻译业务语言** `defaultSourceLang` / `defaultTargetLang`：不变
- 动态文案（含 `%s`、`%d`）在 XML 用 `%1$s` 占位，Kotlin 传参

---

## 资源命名约定

- 格式：`{屏幕}_{组件}_{语义}`，全小写 + 下划线  
- 示例：  
  - `file_editor_export_confirm_title`  
  - `file_projects_delete_message`  
  - `settings_ui_language_label`  
- 按 **功能模块** 分文件（可选二期）：`strings_file_editor.xml` 需查 CMP 是否支持多文件；首期单文件 `strings.xml` 即可。

---

## 待迁移文案范围（盘点）

| 模块 | 主要文件 | 备注 |
|------|----------|------|
| 根导航 | `AppRoot.kt` | 搜索占位、Settings CD |
| Dashboard | `QuickTranslateSection.kt`, `FileProjectsSection.kt`, `DashboardInsightSection.kt`, `EnginePickerDialog.kt`, `LanguagePickerDialog.kt` | 中英混排 |
| 文件编辑 | `FileEditorScreen.kt`, `FileEditorProgressSection.kt`, `FileEditorActionsCard.kt`, `XmlEntryRow.kt` | 含对话框 |
| 文件浏览 | `FilesScreen.kt`, `FileBrowserScreen.kt`, `FileListSection.kt`, … | |
| 设置 | `SettingsScreen.kt`, `SettingsComponents.kt` | 新增界面语言项 |
| 关于 | `AboutSections.kt` | 可选后期 |
| 杂项 | `TranslateScreens.kt`, `FileProjectCard.kt`（`formatModifiedAgo`） | |
| 错误 UI | `TranslationFailureUi.kt` | 需 `StringResource` 或接受 `Locale` 的工厂 |
| Snackbar | `FileEditorScreen.kt`, `MainDashboardScreen.kt` 等 | |

**粗估：** `ui` 包内约 **15+** 个 Kotlin 文件含硬编码 `Text("...")` 或中文提示。

---

## Gradle / 生成

- 已有：`implementation(libs.compose.components.resources)`
- 计划补充（若生成类不可见）：`compose.resources { publicResClass = true }` 于 `composeApp/build.gradle.kts`
- 生成包名以构建为准（通常为 `androidrestranslator.composeapp.generated.resources`）

---

## 测试策略

1. **无网络单测**：不强制测 `stringResource`（需 Compose 环境）；可测 `AppLocale` 解析与 settings 持久化往返。
2. **手工矩阵**：Desktop `run`，Settings 切换 en/zh，抽查 Dashboard、文件编辑、删除对话框、导出确认、翻译错误提示。
3. **回归**：翻译流程、长按删除项目、导出逻辑不受文案迁移影响。

---

## 风险与缓解

| 风险 | 缓解 |
|------|------|
| CMP 版本下 Locale API 文档不全 | 以 Compose 1.10 官方 Resources 示例为准；封装 `AppStrings` 门面便于替换 |
| 字符串遗漏 | 用 IDE 搜索 `Text("`、`title = { Text(` 做 checklist |
| `toUserMessage()` 非 Composable | 提供 `toUserMessage(locale)` 或 `getString(Res, locale)` 包装 |
| 中英混排回归 | 默认 `values/` 统一英文；`values-zh` 全覆盖 |

---

## 验收标准

- [ ] Settings 可切换界面语言 en/zh，重启后保持
- [ ] 主要屏幕（Dashboard、文件编辑、文件项目、设置）无硬编码 UI 文案（About 可例外登记）
- [ ] Desktop / Android 至少各验证一种语言
- [ ] README 增加「界面语言」说明（可选一行指向本 spec）
