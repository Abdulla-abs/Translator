# 阶段二：Compose 多平台 UI 设计说明

**文档类型**：产品 / 交互与架构（阶段二范围）  
**日期**：2026-05-15  
**依赖**：[阶段一设计 v0.3+](./2026-05-14-android-trans-phase1-design.md)（`core.*` 已实现）  
**目标平台**：Android、Desktop（JVM）优先；iOS 共享导航与大部分 UI，文件选择与持久化按平台 `actual` 分岔。

---

## 1. 范围与原则

### 1.1 本阶段交付（与你提供的需求对齐）

| 模块 | 说明 |
|------|------|
| **设置** | 各厂商密钥/参数（键名与 `config.properties.example` 一致）；翻译全局选项（默认源/目标语言、合并策略 Filled / AllReplace、可选「强制翻译 translatable=false」与阶段一 `AbsTranslationConsumer.forceTranslation` 对齐）。 |
| **翻译 · 文本** | 单词/句子：输入原文、源/目标语言码、`TranslatePlainTextUseCase` + `buildDefaultOrchestrator(SecretsProvider)` 出结果与错误提示。 |
| **翻译 · 文件** | `strings.xml` → 另一语言 `strings.xml`：读入 UTF-8 文本 → `TranslateStringsXmlUseCase` + 选定 Consumer → 输出文本；提供「保存为…」或导出路径（桌面文件对话框；Android 用 SAF `createDocument` 或分享，首版可简化为复制到剪贴板 + 说明）。 |
| **关于** | 应用名、版本号、`README` / 开源说明链接、隐私提示（密钥仅存本机）。 |

### 1.2 非目标（可列后续里程碑）

- 整包 `res` 目录树翻译（`TranslateValuesFolderUseCase` + 真实 `FileTreePort`）首版**不强制**；可在「文件翻译」进阶入口或阶段 2.1 做「选择文件夹」。
- XLS / jxl（阶段 1.5）不在本 spec。
- 账号云同步、多用户。

### 1.3 架构约束（延续阶段一）

- **UI → ViewModel → 已有 core 用例 / Orchestrator**；不在 Composable 里直接 new `HttpClient`。
- **密钥与设置**：`commonMain` 定义 `AppSettingsRepository`（`expect`），`androidMain` / `jvmMain` / `iosMain` 用 **DataStore Preferences**（Android）、**Properties 文件或 mmap 文件**（Desktop）、**NSUserDefaults**（iOS）等 `actual`；键名与 `SecretsProvider` 读取键一致，UI 保存后 `SecretsProvider` 从同一数据源读取（或由 Repository 同时实现 `SecretsProvider` 适配器）。
- **导航**：`Material3` + `NavigationSuite`（或 `NavHost` + bottom bar），三主入口：**翻译** | **设置** | **关于**；「翻译」内 **子页签：文本 | 文件**。

---

## 2. 信息架构与导航

```
App
├── 翻译（默认首页）
│   ├── 文本：源文、源语言、目标语言、结果、错误
│   └── 文件：源 XML、目标语言、策略、预览/保存
├── 设置
│   ├── 厂商密钥分组（腾讯 / 百度 / …）
│   └── 全局：默认源/目标语、Filled/AllReplace、强制翻译开关
└── 关于
```

---

## 3. 设置页：数据模型

### 3.1 与 `config.properties` 对齐的键（展示名可中文）

与根目录 `config.properties.example` 一致，包括但不限于：`lingvanex.token`、`tencent.secretId` / `tencent.secretKey` / 可选 `tencent.region`、`baidu.*`、`youdao.*`、`huoshan.*`。

### 3.2 全局翻译设置（建议键）

| 键 | 类型 | 默认 | 说明 |
|----|------|------|------|
| `ui.defaultSourceLang` | String | `en` | 文本/文件翻译默认源语言码 |
| `ui.defaultTargetLang` | String | `zh` | 默认目标语言码 |
| `ui.consumerMode` | enum | `FILLED` | `FILLED` / `ALL_REPLACE` → `FilledTranslationConsumer` / `AllReplaceTranslationConsumer` |
| `ui.forceTranslation` | Boolean | `false` | 对应 `forceTranslation` |

敏感字段写入 **平台安全存储**；Android 可用 `EncryptedFile` 或 DataStore + 系统级防护；Desktop 首版可用 **用户目录下应用私有文件** + 文档说明勿共享磁盘；后续再加强。

---

## 4. 翻译页

### 4.1 文本翻译

- ViewModel 持有 `TranslatePlainTextUseCase`（注入 `buildDefaultOrchestrator(settingsAsSecretsProvider())` 或单独 `TranslationOrchestrator` 工厂）。
- 协程 `viewModelScope` / `rememberCoroutineScope` 调用 `translate`；展示 `TranslationOutcome`（成功文案 / 失败原因）。

### 4.2 文件翻译（strings.xml）

- 输入：多行 `TextField` 粘贴 XML **或**「选择文件」读入字符串（`expect suspend fun readXmlFromUserPickedFile(): String?`）。
- 若目标侧无 XML，传空串给 `TranslateStringsXmlUseCase`（与单测一致）。
- 输出：`TextField` 只读预览 + 「复制」+ 桌面「另存为…」（`expect`）。

---

## 5. 关于页

- `BuildKonfig` 或 `gradle` 注入 `versionName`；静态文案：阶段一/二说明、GitHub 链接（若适用）、**勿将密钥提交仓库**。

---

## 6. 实现顺序建议（供后续 implementation plan）

1. `AppSettingsRepository` + 各端 `actual` + 测试 Fake。  
2. `AppViewModel` / 分屏 ViewModel：`SettingsViewModel`、`TranslateTextViewModel`、`TranslateFileViewModel`。  
3. 导航壳 + 占位页 → 替换为真实表单。  
4. 文件选择 `expect`/`actual`（Desktop 优先）。  
5. 主题与无障碍（可选）。

---

## 7. 自审

- 无与阶段一 **core 反向依赖 UI**（仅 ViewModel 依赖 core）。  
- 密钥不落日志；敏感输入框 `password` 视觉。  
- 与阶段一 spec **§1.3** 一致：本阶段才上 Compose 与文件交互。

---

## 8. 修订记录

| 版本 | 日期 | 说明 |
|------|------|------|
| 0.1 | 2026-05-15 | 初稿：设置 / 文本与文件翻译 / 关于；导航与 Repository 约束 |
