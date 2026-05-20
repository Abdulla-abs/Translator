# AndroidResTranslator

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3-blue.svg)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10-4285F4.svg)](https://www.jetbrains.com/compose-multiplatform/)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Desktop%20(JVM)%20%7C%20iOS-lightgrey.svg)](#支持平台)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

**AndroidResTranslator**（应用内名 *KMP Translator*）是一款基于 **Kotlin Multiplatform** 与 **Compose Multiplatform** 的跨平台工具，用于批量翻译 Android **`strings.xml`** 资源，并管理翻译进度与导出。

> **AndroidResTranslator** is a cross-platform desktop/mobile app for batch-translating Android `strings.xml` resources, with multi-vendor translation APIs and project persistence.

---

## 功能特性

- **批量翻译 `strings.xml`**：解析源 XML，按条目调用翻译 API，生成目标语言 XML
- **多翻译厂商**：火山引擎、Lingvanex、百度、有道、腾讯（可配置密钥，支持首选引擎）
- **文件翻译工作台**：进度统计（Total / Translated / Pending / Error）、源/目标语言选择与互换、条目筛选与单条重试
- **快速翻译**：首页短文本试译，便于验证密钥与语言对
- **项目持久化**：上传/最近项目保存在本地目录（`source.xml`、`result.xml`、`session.json`），退出应用后可恢复会话与错误状态
- **导出**：翻译完成后导出 XML；存在失败条目时会提示是否继续导出（失败条目保留源文）
- **文件浏览**：浏览本地目录并打开 `strings.xml` 进入编辑器
- **主题与设置**：多套 Material 3 外观、默认语言、翻译模式（仅补全 / 全量替换）等

---

## 支持平台

| 平台 | 状态 | 说明 |
|------|------|------|
| **Desktop (JVM)** | 推荐 | Windows / macOS / Linux，`composeApp:run` |
| **Android** | 支持 | `assembleDebug` 或 IDE 运行 |
| **iOS** | 支持 | 使用 Xcode 打开 `iosApp` 构建运行 |

---

## 支持的翻译服务

需在设置或 `config.properties` 中配置对应 API 密钥（**至少配置一家**）：

| 引擎 | 配置项 |
|------|--------|
| Lingvanex | `lingvanex.token` |
| 火山引擎 | `huoshan.accessKeyID`, `huoshan.secretAccessKey` |
| 百度翻译 | `baidu.appId`, `baidu.secretKey` |
| 有道翻译 | `youdao.appId`, `youdao.secretKey` |
| 腾讯翻译 | `tencent.secretId`, `tencent.secretKey`（可选 `tencent.region`） |

默认编排顺序（无首选引擎时）：**火山 → Lingvanex → 百度 → 有道 → 腾讯**。在设置中选择首选引擎后，文件翻译会**优先使用该厂商**，且该厂商失败时**不会**自动回退到其他厂商（避免误显示为「腾讯未配置」等误导信息）。

厂商语言白名单、调用链与 `zh`/`en` 等行为说明见 **[TRANSLATION_VENDOR.md](./TRANSLATION_VENDOR.md)**。

---

## 快速开始

### 环境要求

- **JDK 11+**
- **Android SDK**（仅构建 Android 时需要）
- **Xcode**（仅构建 iOS 时需要，macOS）

### 克隆仓库

```bash
git clone https://github.com/abbas/android-res-translator.git
cd android-res-translator
```

### 配置 API 密钥

1. 复制示例配置：

   ```bash
   cp config.properties.example config.properties
   ```

2. 编辑根目录 **`config.properties`**，填入至少一个厂商的真实密钥。

3. **切勿**将 `config.properties` 提交到 Git（已在 `.gitignore` 中忽略）。

也可在应用内 **Settings** 页面填写并保存密钥（与 `config.properties` 键名一致）。

### 运行桌面版（推荐）

**Windows**

```bat
.\gradlew.bat :composeApp:run
```

**macOS / Linux**

```bash
./gradlew :composeApp:run
```

或在 IntelliJ IDEA / Android Studio 中选择 Gradle 任务 **`composeApp` → `run`**。

### 构建 Android

```bash
# Windows
.\gradlew.bat :composeApp:assembleDebug

# macOS / Linux
./gradlew :composeApp:assembleDebug
```

### 构建 iOS

在 Xcode 中打开 [`iosApp`](./iosApp) 目录，选择目标设备或模拟器运行。

---

## 使用说明

1. **配置密钥**：Settings 中填写翻译厂商 API，并选择首选引擎。
2. **导入 XML**：在 Dashboard 上传 `strings.xml`，或在 Files 页浏览本地文件打开。
3. **选择语言**：在文件编辑页选择源语言与目标语言（可与 Android `values-xx` 目录语言码对应，如 `en`、`zh`、`zh-rTW`）。
4. **开始翻译**：点击 **Start Translation**；可在条目列表中查看状态，失败条目可单独 **Retry**。
5. **导出**：全部成功后可导出；若存在 **Error** 条目，导出前会弹出确认对话框。

翻译项目默认保存在各平台应用数据目录下的 `translation-projects/`（含 `index.json`、各项目的 `source.xml` / `result.xml` / `session.json`）。

---

## 项目结构

```
AndroidResTranslator/
├── composeApp/                 # KMP 主模块（UI + 核心逻辑）
│   └── src/
│       ├── commonMain/         # 共享 UI、翻译编排、XML 编解码、持久化
│       ├── androidMain/
│       ├── jvmMain/            # 桌面入口 Main.kt
│       └── iosMain/
├── iosApp/                     # iOS 壳工程
├── config.properties.example   # 密钥配置模板
├── TRANSLATION_VENDOR.md       # 厂商调用链与语言码说明
└── gradle/
```

核心包说明：

| 路径 | 职责 |
|------|------|
| `core/translation/` | `TranslationOrchestrator`、各厂商 Vendor |
| `core/resources/` | `strings.xml` 解析、Consumer、批量翻译用例 |
| `persistence/` | 项目目录、`session.json`、索引 |
| `ui/screens/fileeditor/` | 文件翻译编辑页 |
| `ui/screens/main/` | Dashboard、快速翻译、最近项目 |

---

## 开发与测试

```bash
# 运行单元测试（commonTest + jvmTest）
./gradlew :composeApp:jvmTest

# 仅编译 JVM / iOS（Windows 使用 gradlew.bat）
./gradlew :composeApp:compileKotlinJvm :composeApp:compileKotlinIosSimulatorArm64
```

测试中使用 `FakeSecretsProvider` 注入假密钥，**不依赖**本机 `config.properties`。

可选：JVM 实网冒烟测试需 `-Dlive.vendor.smoke=true` 及环境变量（见 `LiveVendorSmokeJvmTest.kt`），CI 默认不触网。

调试翻译链路时，可在 Run 控制台查看 `[AndroidResTranslator]` 前缀日志；通过 `TranslationDebugLog.enabled = false` 关闭。

---

## 常见问题

**Q: 已配置 Lingvanex，仍提示腾讯未配置？**  
A: 请确认在设置中选择了 **Lingvanex** 为首选引擎，并使用最新代码（首选厂商失败时不再回退到其他未配置厂商）。

**Q: Lingvanex 返回 “bind a payment method”？**  
A: 这是 Lingvanex 账户计费问题，需在 [Lingvanex](https://lingvanex.com) 控制台绑定支付方式，与语言码无关。

**Q: 退出应用后卡片显示已完成，但条目未翻译？**  
A: 旧版本可能将未译条目误写入 `result.xml`。新版本已修复持久化逻辑；若遇旧数据，请对该项目执行「重新翻译」或重新导入源文件。

---

## 安全提示

- API 密钥仅保存在本机（`config.properties` 或应用设置存储），请自行保管，不要提交到公开仓库。
- 翻译请求会发送到您配置的第三方厂商服务器，使用前请阅读各厂商服务条款与计费说明。

---

## 贡献

欢迎提交 Issue 与 Pull Request。提交前请：

1. 运行 `./gradlew :composeApp:jvmTest` 确保测试通过  
2. 不要包含 `config.properties` 或任何真实密钥  
3. 大型改动请先开 Issue 讨论

---

## 相关文档

- [TRANSLATION_VENDOR.md](./TRANSLATION_VENDOR.md) — 翻译厂商调用链与语言支持  
- [config.properties.example](./config.properties.example) — 配置项说明  

---

## 许可证

本项目采用 **[MIT License](./LICENSE)** 开源。

Copyright (c) 2026 abbas

您可以自由使用、修改与分发本软件，但须保留版权声明与许可全文。详见 [LICENSE](./LICENSE) 文件。

---

## 致谢

基于 [Kotlin Multiplatform](https://kotlinlang.org/multiplatform/)、[Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)、[Ktor](https://ktor.io)、[xmlutil](https://github.com/pdvrieze/xmlutil) 等开源项目构建。
