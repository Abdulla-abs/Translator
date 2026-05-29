# AndroidResTranslator

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3-blue.svg)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10-4285F4.svg)](https://www.jetbrains.com/compose-multiplatform/)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Desktop%20(JVM)%20%7C%20iOS-lightgrey.svg)](#supported-platforms)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

**English** · [简体中文](./README.zh-CN.md)

**AndroidResTranslator** (in-app name: *KMP Translator*) is a **Kotlin Multiplatform** and **Compose Multiplatform** desktop/mobile tool for Android localization workflows: batch-translating **`strings.xml`**, comparing two XML files, and managing multi-language **`res/`** folders—with progress tracking, versioning, and export.

**Repository:** [github.com/Abdulla-abs/Translator](https://github.com/Abdulla-abs/Translator)

---

## Screenshots

### Desktop (Windows)

| Dashboard | Files |
|:---:|:---:|
| ![Dashboard](screenshot/windows/w_dashboard.png) | ![Files](screenshot/windows/w_files.png) |

| About | Settings |
|:---:|:---:|
| ![About](screenshot/windows/w_about.png) | ![Settings](screenshot/windows/w_settings.png) |

| File editor | XML compare | Multi-file import compare |
|:---:|:---:|:---:|
| ![File editor](screenshot/windows/FileEditorScreen.png) | ![XML compare](screenshot/windows/CompareDetail.png) | ![Multi-file import compare](screenshot/windows/ResMultiImportCompare.png) |

### Android

| Dashboard | Files |
|:---:|:---:|
| ![Dashboard](screenshot/android/a_dashboard.jpg) | ![Files](screenshot/android/a_files.jpg) |

| About | Settings |
|:---:|:---:|
| ![About](screenshot/android/a_about.jpg) | ![Settings](screenshot/android/a_settings.jpg) |

---

## App navigation

The app has four main tabs (bottom bar on narrow screens, permanent sidebar on wide screens ≥768dp):

| Tab | Purpose |
|-----|---------|
| **Translate** | Dashboard: quick translate, file projects, XML compare, multi-file `res` projects |
| **Files** | Browse local folders and open `strings.xml` in the file editor |
| **Settings** | API keys, default languages, UI language, themes, quick-translate behavior |
| **About** | Version info and open-source licenses |

---

## Features

### 1. Single-file translation (File Projects)

Batch-translate one **`strings.xml`** at a time. Two workflow modes (chosen at upload on the Dashboard):

| Mode | Upload on Dashboard | In editor | Merge behavior |
|------|---------------------|-----------|----------------|
| **Full replace** | Source `strings.xml` only | Upload **target** `strings.xml`, then translate | Overwrite entries from target baseline |
| **Incremental** | Source `strings.xml` only | Upload **target** `strings.xml` as baseline, then translate | Only fill keys missing or empty in target |

**Editor capabilities**

- Progress: Total / Translated / Pending / Error
- Source & target language pickers (Android-style codes: `en`, `zh`, `zh-rTW`, …) with swap
- Key filter, per-entry retry, pause / resume
- **Retranslate** when export-ready (with confirmation)
- **Export** translated XML; confirm dialog if any entries failed (failed rows keep source text)
- **Project settings**: force-translate entries marked `translatable="false"`
- Translation continues in the background after leaving the editor (`FileEditorControllerStore`)

**Input**: click upload cards, drag-and-drop XML (desktop), or open from the **Files** tab.

![Single-file translation editor](screenshot/windows/FileEditorScreen.png)

### 2. Quick translate (Dashboard)

- Trial short text with current source/target languages
- Pick **preferred translation engine** on the quick-translate card
- **Auto-translate** after debounce, or manual button only (Settings → Translation Strategies)
- Copy result to clipboard

### 3. File compare (Compare Projects)

Side-by-side diff of **two** `strings.xml` files (no API calls).

- Create a named compare project → upload file A and file B
- Sticky grid: `key` column + left/right values; differences highlighted
- Tap a cell to copy its text to the clipboard
- Re-upload either side to refresh the comparison

![XML compare detail](screenshot/windows/CompareDetail.png)

### 4. Multi-file `res` projects (Multi-file Projects)

Import an entire Android **`res/`** tree (`values/`, `values-en/`, …) and manage all `strings.xml` files together.

- **Initialize**: pick the folder that contains `values*` directories
- **Export**: full workbook (`.xlsx`), or single-language `.xlsx` / `.xml`
- **Import spreadsheet to compare**: load `.xlsx`, diff against workspace, optionally **apply** changed cells back
- **Version history**: push named version, restore, delete version (and successors)
- Dirty-state hint when workspace differs from last saved version

> Batch AI translation for multi-file projects is planned; export/import/compare/versioning is available today.

![Multi-file res import compare](screenshot/windows/ResMultiImportCompare.png)

### 5. Files tab

- Directory browser for local `strings.xml`
- Search box in the top bar to filter listed projects
- Opens the same file editor as Dashboard projects

### 6. Translation providers

Configure at least one provider in **Settings** or root **`config.properties`**:

| Provider | Config keys |
|----------|-------------|
| Lingvanex | `lingvanex.token` |
| Volcengine (Huoshan) | `huoshan.accessKeyID`, `huoshan.secretAccessKey` |
| Baidu | `baidu.appId`, `baidu.secretKey` |
| Youdao | `youdao.appId`, `youdao.secretKey` |
| Tencent | `tencent.secretId`, `tencent.secretKey` (optional `tencent.region`) |

**Fallback order** (no preferred engine): Huoshan → Lingvanex → Baidu → Youdao → Tencent.

**Preferred engine** (Quick translate card or persisted setting): file translation uses that vendor first and **does not** silently fall back to others on failure (avoids misleading errors like “Tencent not configured”).

See **[TRANSLATION_VENDOR.md](./TRANSLATION_VENDOR.md)** for vendor chains, language whitelists, and code mapping.

### 7. Settings & appearance

- Provider API keys (same names as `config.properties`)
- Default source/target languages for new projects
- **UI language**: English or 简体中文 (app chrome only, not `strings.xml` languages)
- **Themes**: Classic (dark purple), Geek Abyss, Minimalist Porcelain (light)
- Quick translate: auto vs manual trigger

### 8. Persistence

Projects are stored under the platform app data directory:

| Type | Folder (relative to app data root) | Main files |
|------|-----------------------------------|------------|
| File translation | `translation-projects/<id>/` | `source.xml`, `target-baseline.xml` (incremental), `result.xml`, `session.json` |
| Compare | `compare-projects/<id>/` | Left/right XML copies |
| Multi-file `res` | `res-multi-projects/<id>/` | Workspace copy, `meta.json`, version snapshots |

**Desktop (JVM)** default root: `~/.android_res_translator/`

Index files (`index.json`) list projects per type. Sessions survive app restarts (progress, errors, languages).

---

## Supported platforms

| Platform | Status | Notes |
|----------|--------|-------|
| **Desktop (JVM)** | Recommended | Windows / macOS / Linux — `composeApp:run` |
| **Android** | Supported | `assembleDebug` or run from IDE |
| **iOS** | Supported | Open `iosApp` in Xcode |

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

## Usage guide

### A. First-time setup

1. Open **Settings** → enter API keys for at least one translation provider.
2. (Optional) Set default source/target languages, UI language, and theme under **Translation Strategies**.
3. On the **Translate** tab, use **Quick translate** to verify keys and pick a **preferred engine**.

### B. Translate a single `strings.xml` (full replace)

1. **Translate** → **File Projects** → **Upload XML (Full Replace)** → select source `strings.xml`.
2. In the editor, upload the **target** `strings.xml` (existing translation file to overwrite).
3. Set source/target languages → **Start Translation**.
4. Retry failed rows individually if needed → **Export** when ready.

### C. Translate a single `strings.xml` (incremental)

1. **Translate** → **Upload XML (Incremental)** → select source `strings.xml`.
2. In the editor, upload the **target baseline** (current production `strings.xml`).
3. The app plans: translate missing/empty keys, skip keys already present in target.
4. Start translation → export merged result.

### D. Open XML from disk (Files tab)

1. **Files** → browse to your project → open `strings.xml`.
2. Same editor as Dashboard; useful for files not yet in “recent projects”.

### E. Compare two XML files

1. **Translate** → **File Compare** → **Create compare project** → enter a name.
2. Upload file A and file B → **Compare**.
3. Review the diff grid; tap cells to copy values.

### F. Multi-language `res` folder workflow

1. **Translate** → **Multi-file Projects** → create project → open it.
2. **Select res folder** (parent of `values`, `values-en`, …) and wait for import.
3. **Export full spreadsheet** for offline editing, or export one language as `.xml`/`.xlsx`.
4. **Import spreadsheet to compare** → review diffs → **Apply** to write changes into the workspace.
5. **Push version** before large edits; **restore** or **delete** versions from the version list.

### G. Export with errors

If any entries are in **Error** state, export shows a confirmation: continuing keeps source text for failed keys.

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
| `core/resources/` | `strings.xml` codec, planners, batch use cases |
| `core/resources/compare/` | Two-file compare matrix |
| `core/resources/resmulti/` | `res/` scan, export, import-compare |
| `persistence/` | Project folders, `session.json`, indexes |
| `ui/screens/fileeditor/` | Single-file translation editor |
| `ui/screens/main/` | Dashboard, quick translate |
| `ui/screens/compare/` | XML compare projects |
| `ui/screens/resmulti/` | Multi-file `res` projects |

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
A: Select **Lingvanex** as the preferred engine on the quick-translate card (or in settings) and use a recent build. Preferred-vendor failures no longer fall back to unconfigured vendors.

**Q: Lingvanex returns “bind a payment method”?**  
A: Lingvanex billing issue—bind a payment method in the [Lingvanex](https://lingvanex.com) console; not a language-code bug.

**Q: The home card shows “complete” after restart but entries were not translated?**  
A: Older builds could write source text into `result.xml` for pending entries. Newer builds fix persistence; use **retranslate** or re-import the source file for affected projects.

**Q: Incremental project won’t start translation?**  
A: Upload the **target baseline** `strings.xml` in the editor first; incremental mode requires a target file before translating or exporting.

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
