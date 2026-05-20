# AndroidResTranslator

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3-blue.svg)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10-4285F4.svg)](https://www.jetbrains.com/compose-multiplatform/)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Desktop%20(JVM)%20%7C%20iOS-lightgrey.svg)](#supported-platforms)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

**English** · [简体中文](./README.zh-CN.md)

**AndroidResTranslator** (in-app name: *KMP Translator*) is a **Kotlin Multiplatform** and **Compose Multiplatform** app for batch-translating Android **`strings.xml`** resources, with progress tracking and export.

**Repository:** [github.com/Abdulla-abs/Translator](https://github.com/Abdulla-abs/Translator)

---

## Screenshots

### Desktop (Windows)

| Dashboard | Files | Settings |
|:---:|:---:|:---:|
| ![Dashboard](screenshot/windows/w_dashboard.png) | ![Files](screenshot/windows/w_files.png) | ![Settings](screenshot/windows/w_settings.png) |

| About |
|:---:|
| ![About](screenshot/windows/w_about.png) |

### Android

| Dashboard | Files | Settings |
|:---:|:---:|:---:|
| ![Dashboard](screenshot/android/a_dashboard.jpg) | ![Files](screenshot/android/a_files.jpg) | ![Settings](screenshot/android/a_settings.jpg) |

| About |
|:---:|
| ![About](screenshot/android/a_about.jpg) |

---

## Features

- **Batch `strings.xml` translation** — parse source XML, translate entries via APIs, emit target-language XML
- **Multiple vendors** — Volcengine (Huoshan), Lingvanex, Baidu, Youdao, Tencent (configure API keys; optional preferred engine)
- **File translation workspace** — progress (Total / Translated / Pending / Error), source/target language pickers with swap, key filter, per-entry retry
- **Quick translate** — short text trial on the dashboard to verify keys and language pairs
- **Project persistence** — uploads and recent projects stored locally (`source.xml`, `result.xml`, `session.json`); resume session and error state after restart
- **Export** — export XML when done; confirm dialog if any entries failed (failed entries keep source text)
- **File browser** — open local `strings.xml` in the editor
- **Themes & settings** — Material 3 skins, default languages, translation mode (fill missing only / replace all)

---

## Supported platforms

| Platform | Status | Notes |
|----------|--------|-------|
| **Desktop (JVM)** | Recommended | Windows / macOS / Linux — `composeApp:run` |
| **Android** | Supported | `assembleDebug` or run from IDE |
| **iOS** | Supported | Open `iosApp` in Xcode |

---

## Translation providers

Configure at least one provider in **Settings** or root **`config.properties`**:

| Provider | Config keys |
|----------|-------------|
| Lingvanex | `lingvanex.token` |
| Volcengine (Huoshan) | `huoshan.accessKeyID`, `huoshan.secretAccessKey` |
| Baidu | `baidu.appId`, `baidu.secretKey` |
| Youdao | `youdao.appId`, `youdao.secretKey` |
| Tencent | `tencent.secretId`, `tencent.secretKey` (optional `tencent.region`) |

Default fallback order (when no preferred engine is set): **Huoshan → Lingvanex → Baidu → Youdao → Tencent**.

When you choose a **preferred engine** in Settings, file translation uses that vendor first. If it fails, the app **does not** silently fall back to other vendors (so you see the real error instead of e.g. “Tencent not configured”).

See **[TRANSLATION_VENDOR.md](./TRANSLATION_VENDOR.md)** for vendor chains, language whitelists, and code mapping.

---

## Quick start

### Requirements

- **JDK 11+**
- **Android SDK** (Android builds only)
- **Xcode** (iOS builds only, macOS)

### Clone

```bash
git clone https://github.com/Abdulla-abs/Translator.git
cd Translator
```

### API keys

1. Copy the sample config:

   ```bash
   cp config.properties.example config.properties
   ```

2. Edit **`config.properties`** at the repo root with real credentials for at least one provider.

3. **Do not** commit `config.properties` (listed in `.gitignore`).

You can also enter keys in the in-app **Settings** screen (same key names as `config.properties`).

### Run desktop (recommended)

**Windows**

```bat
.\gradlew.bat :composeApp:run
```

**macOS / Linux**

```bash
./gradlew :composeApp:run
```

Or run the Gradle task **`composeApp` → `run`** from IntelliJ IDEA / Android Studio.

### Build Android

```bash
# Windows
.\gradlew.bat :composeApp:assembleDebug

# macOS / Linux
./gradlew :composeApp:assembleDebug
```

### Build iOS

Open the [`iosApp`](./iosApp) directory in Xcode and run on a device or simulator.

---

## Usage

1. **Configure keys** — add provider credentials in Settings and pick a preferred engine.
2. **Import XML** — upload `strings.xml` on the Dashboard, or open a file from the Files tab.
3. **Languages** — set source and target (Android-style codes such as `en`, `zh`, `zh-rTW`).
4. **Translate** — **Start Translation**; retry failed entries individually.
5. **Export** — export when complete; if there are **Error** entries, confirm before exporting.

Projects are stored under `translation-projects/` in the app data directory (`index.json`, per-project `source.xml` / `result.xml` / `session.json`).

---

## Project layout

```
AndroidResTranslator/
├── composeApp/                 # KMP app module (UI + core)
│   └── src/
│       ├── commonMain/         # Shared UI, translation, XML, persistence
│       ├── androidMain/
│       ├── jvmMain/            # Desktop entry (Main.kt)
│       └── iosMain/
├── iosApp/                     # iOS host
├── config.properties.example
├── TRANSLATION_VENDOR.md
└── gradle/
```

| Package | Role |
|---------|------|
| `core/translation/` | `TranslationOrchestrator`, vendor implementations |
| `core/resources/` | `strings.xml` codec, consumers, batch use cases |
| `persistence/` | Project folders, `session.json`, index |
| `ui/screens/fileeditor/` | File translation editor |
| `ui/screens/main/` | Dashboard, quick translate, recent projects |

---

## Development & testing

```bash
# Unit tests (commonTest + jvmTest)
./gradlew :composeApp:jvmTest

# Compile JVM / iOS only (use gradlew.bat on Windows)
./gradlew :composeApp:compileKotlinJvm :composeApp:compileKotlinIosSimulatorArm64
```

Tests use `FakeSecretsProvider` and do **not** require a local `config.properties`.

Optional live JVM smoke tests: `-Dlive.vendor.smoke=true` and env vars (see `LiveVendorSmokeJvmTest.kt`); CI does not hit the network by default.

For translation debugging, watch the Run console for `[AndroidResTranslator]` logs; set `TranslationDebugLog.enabled = false` to disable.

---

## FAQ

**Q: Lingvanex is configured but I still see “Tencent not configured”?**  
A: Select **Lingvanex** as the preferred engine in Settings and use a recent build (preferred vendor failures no longer fall back to unconfigured vendors).

**Q: Lingvanex returns “bind a payment method”?**  
A: This is a Lingvanex billing issue — bind a payment method in the [Lingvanex](https://lingvanex.com) console; not a language-code bug.

**Q: The home card shows “complete” after restart but entries were not translated?**  
A: Older builds could write source text into `result.xml` for pending entries. Newer builds fix persistence; use **retranslate** or re-import the source file for affected projects.

---

## Security

- API keys stay on your machine (`config.properties` or app settings). Never commit real keys to a public repo.
- Translation requests go to the third-party providers you configure; review their terms and pricing.

---

## Contributing

Issues and pull requests are welcome. Before submitting:

1. Run `./gradlew :composeApp:jvmTest`
2. Do not include `config.properties` or real secrets
3. For large changes, open an issue first

---

## Related docs

- [TRANSLATION_VENDOR.md](./TRANSLATION_VENDOR.md) — vendor chains and language support  
- [config.properties.example](./config.properties.example) — configuration keys  
- [README.zh-CN.md](./README.zh-CN.md) — Chinese documentation  

---

## License

This project is licensed under the **[MIT License](./LICENSE)**.

Copyright (c) 2026 abbas

---

## Acknowledgments

Built with [Kotlin Multiplatform](https://kotlinlang.org/multiplatform/), [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/), [Ktor](https://ktor.io), [xmlutil](https://github.com/pdvrieze/xmlutil), and other open-source libraries.
