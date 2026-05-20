# 厂商翻译调用链与语言支持说明

本文档说明本 KMP 项目中多厂商翻译的调用顺序、如何判断是否支持某一**目标语言**，以及常见语言对（如 `zh` → `en`）在代码中的实际行为。

---

## 1. 架构概览

```
UI / FileEditor / Dashboard
        │
        ▼
TranslatePlainTextUseCase
        │  TranslationRequest(sourceText, sourceLanguage, targetLanguage)
        ▼
TranslationOrchestrator          ← 按厂商顺序尝试，跳过不支持的目标语言
        │
        ├── HuoshanVendor        (火山)
        ├── LingvanexVendor      (Lingvanex / Backenster)
        ├── BaiduVendor          (百度)
        ├── YoudaoVendor         (有道)
        └── TencentVendor        (腾讯)
```

**装配入口**：`composeApp/.../core/translation/DefaultTranslationModule.kt`  
**编排逻辑**：`composeApp/.../core/translation/TranslationOrchestrator.kt`

厂商顺序与旧工程 `TranslationManager` 一致：**火山 → Lingvanex → 百度 → 有道 → 腾讯**。

---

## 2. 核心类型

### 2.1 翻译请求

```kotlin
data class TranslationRequest(
    val sourceText: String,
    val sourceLanguage: String,   // 源语言（Android 资源目录风格，如 "zh"）
    val targetLanguage: String,   // 目标语言（编排器预检只看这一项）
)
```

### 2.2 厂商接口

```kotlin
interface TranslationVendor {
    val name: String
    fun supportsTargetLanguage(isoOrAndroidCode: String): Boolean
    fun supportsLanguagePair(sourceLanguage: String, targetLanguage: String): Boolean
    suspend fun translate(request: TranslationRequest): TranslationOutcome
}
```

**注意**：编排器使用 `supportsLanguagePair` 过滤厂商。默认实现仅检查目标语言；腾讯等厂商可覆盖为源+目标白名单校验。

### 2.3 编排器逻辑

`TranslationOrchestrator.translate()` 对每个厂商：

1. 若 `!vendor.supportsLanguagePair(request.sourceLanguage, request.targetLanguage)` → **跳过**，不发起 HTTP。
2. 否则调用 `vendor.translate(request)`。
3. 第一个返回 `TranslationOutcome.Ok` 的厂商即成功结束。
4. 若全部被跳过或全部失败 → 返回 `TranslationFailure.NoVendorForTarget` 或最后一个 `VendorRejected`。

---

## 3. 各厂商如何检查目标语言

| 厂商 | `supportsTargetLanguage` 实现 | 含义 |
|------|--------------------------------|------|
| **火山** `HuoshanVendor` | `HuoshanLanguageSupport` 白名单 | 全向互译 + 定向限制（见下文） |
| **Lingvanex** `LingvanexVendor` | `ANDROID_TO_LINGVANEX_TARGET.containsKey(code)` | 仅白名单内的 Android 语言码 |
| **百度** `BaiduVendor` | `BaiduLanguageSupport` 白名单 | 列表内语种互译；源可为 `auto` |
| **有道** `YoudaoVendor` | `YoudaoLanguageSupport` 白名单 | 源/目标均须在支持列表；源可为 `auto` |
| **腾讯** `TencentVendor` | `TencentLanguageSupport` 白名单 | 源/目标语言对 + 目标码并集校验（见下文） |

### 3.1 Lingvanex 目标语言白名单

Lingvanex 是唯一在本地维护**明确目标语言表**的厂商（`LingvanexVendor.ANDROID_TO_LINGVANEX_TARGET`），key 为 Android 侧语言/地区码，value 为 Lingvanex API 所需码。

| Android 码 | Lingvanex API 码 |
|------------|------------------|
| `en` | `en_US` |
| `zh` | `zh-Hans_CN` |
| `zh-rHK` / `zh-rMO` / `zh-rTW` | `zh-Hant_TW` |
| `ja` | `ja_JP` |
| `ko` | `ko_KR` |
| `de` | `de_DE` |
| `fr` | `fr_CA` |
| `es` | `es_ES` |
| … | （完整列表见 `LingvanexVendor.kt`） |

翻译时还会再次查表；若目标不在 map 中，返回 `VendorRejected("unsupported target language: …")`。

### 3.2 火山引擎文本翻译白名单

实现见 `HuoshanLanguageSupport.kt`。

- **默认**：列表内 130+ 语种**全向互译**
- **斯洛伐克语 `sk`**：仅支持其它语种 → `sk`（`sk` 不可作源语言）
- **仅→简体中文 `zh`**：`bo`（藏语）、`nan`（闽南语）、`wuu`（吴语）、`yue`（粤语）、`cmn`（西南官话）、`ug`（维吾尔语）
- **Android 别名**：`zh-rTW` → `zh-Hant-tw`，`zh-rHK` → `zh-Hant-hk`，`in` → `id`

### 3.3 腾讯 TMT 源/目标白名单

实现见 `TencentLanguageSupport.kt`（`composeApp/.../vendors/`）。

- **18 种源语言**：`zh`、`zh-TW`、`en`、`ja`、`ko`、`fr`、`es`、`it`、`de`、`tr`、`ru`、`pt`、`vi`、`id`、`th`、`ms`、`ar`、`hi`
- **目标语言**：按源语言维护 `targetsBySource` 映射（与腾讯云文档各源语言目标列表一致）
- **编排器** 调用 `supportsLanguagePair(source, target)` 做严格预检
- **Android 别名**：`zh-rTW` / `zh-rHK` / `zh-rMO` → `zh-TW`，`in` → `id`；请求 API 时使用腾讯云码（如 `zh-TW`）

示例：

| 语言对 | 腾讯是否支持 |
|--------|-------------|
| `zh` → `en` | ✅ |
| `en` → `hi` | ✅ |
| `hi` → `zh` | ❌（印地语源仅支持译往 `en`） |
| `ja` → `fr` | ❌ |

### 3.4 有道翻译语言白名单

实现见 `YoudaoLanguageSupport.kt`。

- **扁平语言表**：官方列表中的全部代码（如 `zh-CHS`、`zh-CHT`、`en`、`sr-Cyrl` 等，共 100+ 种）
- **互译模型**：任意两种已支持语言之间可译（无定向 `targetsBySource` 表）
- **自动识别**：源语言可为 `auto`（`from=auto`），目标须为白名单内代码
- **Android 别名**：`zh` → `zh-CHS`，`zh-rTW` → `zh-CHT`，`in` → `id`，`sr` → `sr-Latn`

### 3.5 百度翻译语言白名单

实现见 `BaiduLanguageSupport.kt`。

- **扁平语种表**：官方列表全部代码（如 `zh`、`cht`、`en`、`jp`、`kor`、`fra`、`auto` 等）
- **互译模型**：白名单内任意两种语种可译（`from != to`）
- **自动检测**：源语言可为 `auto`
- **Android 别名**：`zh-rTW` → `cht`，`ja` → `jp`，`ko` → `kor`，`fr` → `fra`，`es` → `spa` 等

### 3.6 其他厂商

（当前五家厂商均已维护白名单。）

---

## 4. 示例：`zh`（中文）→ `en`（英语）

### 4.1 请求构造

```kotlin
TranslationRequest(
    sourceText = "你好",
    sourceLanguage = "zh",
    targetLanguage = "en",
)
```

UI 侧默认语言来自 `AppSettingsSnapshot.defaultSourceLang` / `defaultTargetLang`（设置页可改）。

### 4.2 预检结果

对 `targetLanguage = "en"`：

| 厂商 | `supportsTargetLanguage("en")` |
|------|-------------------------------|
| 火山 | ✅ `true` |
| Lingvanex | ✅ `true`（map 含 `"en"`） |
| 百度 | ✅ `true` |
| 有道 | ✅ `true` |
| 腾讯 | ✅ `supportsLanguagePair("zh","en")` 为 true |

因此**五个厂商都会进入** `translate()`；**第一个 API 成功**的厂商结果被采用。

### 4.3 实际 HTTP 中的语言字段（节选）

- **百度**：表单字段 `from=zh`, `to=en`
- **腾讯**：JSON `Source` / `Target`
- **有道**：表单 `from` / `to`
- **火山**：`SourceLanguage` / `TargetLanguage`
- **Lingvanex**：请求体 `to=en_US`（由 map 映射）

源语言 `zh` 不做本地预检；若某厂商不支持该源/目标组合，由 API 错误体现。

---

## 5. 失败类型与 UI 文案

| 类型 | 触发场景 |
|------|----------|
| `NoVendorForTarget(targetLanguage)` | 所有厂商均被 `supportsTargetLanguage` 跳过，且没有产生其它失败 |
| `VendorRejected(vendorName, detail)` | 密钥缺失、API 业务错误、Lingvanex 未映射语言等 |
| `NetworkFailure(message)` | 网络异常 |

用户可见文案见 `ui/TranslationFailureUi.kt`，例如：

- `No translation service supports target language: en`

---

## 6. 语言码约定

- 参数名 `isoOrAndroidCode` 表示 **Android `values-xx` 目录风格**（如 `en`、`zh`、`zh-rTW`），与资源翻译场景一致。
- **不是**完整的 BCP-47（如 `en-US`），除非个别厂商内部映射（如 Lingvanex 的 `en_US`）。
- 设置页与翻译请求应使用同一套码，避免 `en` 与 `en-rUS` 混用导致 Lingvanex 查表失败。

---

## 7. 在代码中主动检查「是否支持译成某语言」

### 7.1 检查单个厂商

```kotlin
val lingvanex: LingvanexVendor = // ...
lingvanex.supportsTargetLanguage("en")  // true
lingvanex.supportsTargetLanguage("xx")  // false
```

### 7.2 检查整条链是否有人会尝试

```kotlin
fun anyVendorSupportsTarget(
    vendors: List<TranslationVendor>,
    target: String,
): Boolean = vendors.any { it.supportsTargetLanguage(target) }
```

对 `en` 而言，在当前实现下几乎恒为 `true`（因多数厂商仅检查非空）。

### 7.3 严格能力（语言对是否真能译）

项目**尚未**提供统一的「源+目标」白名单或探测接口。若要 UI 禁用不支持的选项，需要：

- 扩展各厂商文档中的支持列表，或
- 增加 `supportsLanguagePair(from, to)` 与集中注册表，或
- 依赖试译 / API 元数据接口（若厂商提供）。

---

## 8. 相关源码路径

| 说明 | 路径 |
|------|------|
| 厂商接口 | `composeApp/src/commonMain/kotlin/.../core/translation/TranslationVendor.kt` |
| 编排器 | `composeApp/src/commonMain/kotlin/.../core/translation/TranslationOrchestrator.kt` |
| 默认装配 | `composeApp/src/commonMain/kotlin/.../core/translation/DefaultTranslationModule.kt` |
| 用例入口 | `composeApp/src/commonMain/kotlin/.../core/text/TranslatePlainTextUseCase.kt` |
| 模型与失败类型 | `composeApp/src/commonMain/kotlin/.../core/translation/TranslationModels.kt` |
| 各厂商实现 | `composeApp/src/commonMain/kotlin/.../core/translation/vendors/` |
| 腾讯语言白名单 | `composeApp/src/commonMain/kotlin/.../core/translation/vendors/TencentLanguageSupport.kt` |
| 有道语言白名单 | `composeApp/src/commonMain/kotlin/.../core/translation/vendors/YoudaoLanguageSupport.kt` |
| 火山语言白名单 | `composeApp/src/commonMain/kotlin/.../core/translation/vendors/HuoshanLanguageSupport.kt` |
| 百度语言白名单 | `composeApp/src/commonMain/kotlin/.../core/translation/vendors/BaiduLanguageSupport.kt` |
| 编排器测试 | `composeApp/src/commonTest/kotlin/.../core/translation/TranslationOrchestratorTest.kt` |
| Lingvanex 语言测试 | `composeApp/src/commonTest/kotlin/.../core/translation/vendors/LingvanexVendorTest.kt` |

---

## 9. 测试

```shell
# Windows
.\gradlew.bat :composeApp:jvmTest

# macOS / Linux
./gradlew :composeApp:jvmTest
```

与语言支持相关的用例包括 `TranslationOrchestratorTest`（跳过不支持厂商）、`LingvanexVendorTest.supportsMappedTargetsOnly` 等。

---

## 10. 已知限制与后续可改进点

1. 五家厂商（火山、Lingvanex、百度、有道、腾讯）均已维护本地白名单。
3. **无统一语言能力注册表**，设置页语言列表与厂商能力未联动。
4. 若需「没有任何厂商支持目标 `xx`」的准确判断，应扩展白名单或增加 `supportsLanguagePair`，而不能仅依赖当前的 `supportsTargetLanguage`。

---

*文档随 `composeApp` 中 `core/translation` 实现维护；若厂商逻辑变更，请同步更新本文档。*
