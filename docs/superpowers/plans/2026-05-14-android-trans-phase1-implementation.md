# AndroidResTranslator 阶段一核心迁移 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development`（推荐）或 `superpowers:executing-plans` 按任务顺序执行。步骤使用 `- [ ]` 勾选跟踪。

**Goal:** 在 `composeApp` 的 `commonMain` 中落地 [阶段一设计 spec v0.3](../specs/2026-05-14-android-trans-phase1-design.md)：**Ktor REST 多厂商链**、**多平台 XML 的 strings.xml 管线**、**Consumer / 差异分析**与 **可重复单测/golden**，不实现 Compose 业务 UI。

**Architecture:** `core.translation`（HTTP 厂商链 + 语言映射）与 `core.resources`（解析/序列化/Consumer/用例）分层；`core.text` 调用 orchestrator；`core.ports` 提供 `expect`/`actual` 的密钥与（可选）文件树；各平台仅补 **Ktor Engine** 与 **SecretsProvider**。

**Tech Stack:** Kotlin 2.3.x、KMP、`io.ktor:ktor-client-*`、`kotlinx-serialization-json`、`io.github.pdvrieze.xmlutil`（多平台 XML，**0.91.1**，原 `nl.adaptivity` 坐标已弃用）、`kotlinx-coroutines`、`kotlin-test`；哈希：`org.kotlincrypto.hash` 系（Baidu MD5、Youdao SHA-256 等）。

**执行进度（Subagent-Driven / 本会话）：**

- [x] **Task 1–2**：版本目录、`kotlinSerialization` 插件、`composeApp` 依赖、各端 `HttpClientFactory`；`:composeApp:compileKotlinJvm` 已通过。Android 编译需本机接受 SDK 许可证（`sdkmanager --licenses`）。xmlutil 实际坐标为 `io.github.pdvrieze.xmlutil:*:0.91.1`（计划正文已同步）。
- [x] **Task 3–6**：`SecretsProvider` / `FakeSecretsProvider`、翻译模型与 `TranslationOutcome`、`TranslationOrchestrator`、`TranslatePlainTextUseCase` 及对应 `commonTest`；`:composeApp:jvmTest` 已通过。
- [x] **Task 7**：`BaiduVendor`（`submitForm` + `MD5` 签名 + kotlinx.serialization 解析）与 `BaiduVendorTest`（MockEngine）。
- [x] **Task 9**：`LingvanexVendor` + `LingvanexVendorTest`（语言表、JSON POST、`Authorization`）。

**输入依据:** 旧工程路径 `android_trans/src/main/java/org/example/`（对照用，终态不依赖 dom4j/Java SDK）。

---

## 文件结构（新建 / 修改清单）

| 路径 | 职责 |
|------|------|
| [gradle/libs.versions.toml](../../gradle/libs.versions.toml) | 增加 ktor、serialization、xmlutil、kotlincrypto 等版本与 `libraries` 条目 |
| [build.gradle.kts](../../build.gradle.kts) | `kotlinSerialization` 插件 `apply false` |
| [composeApp/build.gradle.kts](../../composeApp/build.gradle.kts) | 插件、`commonMain`/`commonTest`/各端 `ktor` engine、`serialization`、`xmlutil` |
| `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/ports/SecretsProvider.kt` | `expect fun secretsProvider(): SecretsProvider` + 接口 |
| `composeApp/src/androidMain/.../SecretsProvider.android.kt` | `actual`（阶段一可先返回内存 map 或读 `BuildConfig`/`assets` 占位） |
| `composeApp/src/iosMain/.../SecretsProvider.ios.kt` | `actual` 占位 |
| `composeApp/src/jvmMain/.../SecretsProvider.jvm.kt` | `actual` 从系统属性或本地文件读（仅开发） |
| `composeApp/src/commonMain/kotlin/.../core/translation/` | 模型、错误、`TranslationOrchestrator`、各 `*Vendor` |
| `composeApp/src/commonMain/kotlin/.../core/text/TranslatePlainTextUseCase.kt` | 通用翻译用例 |
| `composeApp/src/commonMain/kotlin/.../core/resources/` | XML 模型、parser、serializer、consumer、folder 用例、diff |
| `composeApp/src/commonTest/kotlin/.../` | 单测与 golden 资源（可放 `resources/` 下小 xml） |

---

### Task 1: 版本目录与根工程序列化插件

**Files:**

- Modify: [gradle/libs.versions.toml](../../gradle/libs.versions.toml)
- Modify: [build.gradle.kts](../../build.gradle.kts)

- [ ] **Step 1.1: 在 `libs.versions.toml` 追加版本与库坐标**

在 `[versions]` 增加（版本号实现时以 Maven Central 解析为准，下列为可用起点）：

```toml
ktor = "3.0.3"
kotlinx-serialization = "1.8.0"
xmlutil = "0.91.1"
kotlincrypto = "0.5.4"
```

在 `[libraries]` 增加（示例别名，可按项目命名习惯微调）：

```toml
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-android = { module = "io.ktor:ktor-client-android", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-mock = { module = "io.ktor:ktor-client-mock", version.ref = "ktor" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
xmlutil-core = { module = "io.github.pdvrieze.xmlutil:core", version.ref = "xmlutil" }
xmlutil-serialization = { module = "io.github.pdvrieze.xmlutil:serialization", version.ref = "xmlutil" }
kotlincrypto-md = { module = "org.kotlincrypto.hash:md", version.ref = "kotlincrypto" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx-coroutines" }
```

在 `[plugins]` 增加：

```toml
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

- [ ] **Step 1.2: 根 `build.gradle.kts` 注册序列化插件**

在 `plugins { }` 内追加一行：

```kotlin
    alias(libs.plugins.kotlinSerialization) apply false
```

- [ ] **Step 1.3: 验证**

Run: `Set-Location f:\idea_pro\AndroidResTranslator; .\gradlew.bat :composeApp:dependencies --configuration commonMainCompileClasspath`

Expected: 命令成功（若尚未引用新库可能仍成功；Task 2 后再跑一次确认 ktor 出现）。

- [ ] **Step 1.4: Commit**

```bash
git add gradle/libs.versions.toml build.gradle.kts
git commit -m "build: add ktor, serialization, xmlutil, kotlincrypto versions for phase1 core"
```

---

### Task 2: composeApp 启用序列化与各源集 Ktor / XML 依赖

**Files:**

- Modify: [composeApp/build.gradle.kts](../../composeApp/build.gradle.kts)

- [ ] **Step 2.1: `plugins` 块增加**

```kotlin
    alias(libs.plugins.kotlinSerialization)
```

- [ ] **Step 2.2: `kotlin { }` 内 `compilerOptions` 如需可保持默认；在 `commonMain.dependencies` 追加**

```kotlin
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.xmlutil.core)
            implementation(libs.xmlutil.serialization)
            implementation(libs.kotlincrypto.md)
            implementation(libs.kotlincrypto.sha2)
```

- [ ] **Step 2.3: 各平台 engine**

`androidMain.dependencies { implementation(libs.ktor.client.android) }`  
`iosArm64().binaries` 已存在；在 `iosArm64Main.dependencies` / `iosSimulatorArm64Main.dependencies` 增加 `implementation(libs.ktor.client.darwin)`（与模板中 sourceSets 名称一致即可）。  
`jvmMain.dependencies { implementation(libs.ktor.client.cio) }`

- [ ] **Step 2.4: 提供 `HttpClient` 工厂（新建）**

Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/translation/HttpClientFactory.kt`

```kotlin
package `fun`.abbas.android_res_translator.core.translation

import io.ktor.client.HttpClient

/** 各平台安装 Ktor Engine；common 内仅声明，由 actual 配置 ContentNegotiation + Json。 */
expect fun createJsonHttpClient(): HttpClient
```

Create: `composeApp/src/androidMain/kotlin/fun/abbas/android_res_translator/core/translation/HttpClientFactory.android.kt`

```kotlin
package `fun`.abbas.android_res_translator.core.translation

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual fun createJsonHttpClient(): HttpClient = HttpClient(Android) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true })
    }
}
```

Create: `composeApp/src/jvmMain/kotlin/fun/abbas/android_res_translator/core/translation/HttpClientFactory.jvm.kt`

```kotlin
package `fun`.abbas.android_res_translator.core.translation

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual fun createJsonHttpClient(): HttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true })
    }
}
```

Create: `composeApp/src/iosMain/kotlin/fun/abbas/android_res_translator/core/translation/HttpClientFactory.ios.kt`

```kotlin
package `fun`.abbas.android_res_translator.core.translation

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual fun createJsonHttpClient(): HttpClient = HttpClient(Darwin) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true })
    }
}
```

- [ ] **Step 2.5: 编译**

Run: `.\gradlew.bat :composeApp:compileKotlinIosSimulatorArm64` 与 `.\gradlew.bat :composeApp:compileDebugKotlinAndroid`（或 `compileKotlinMetadata` / `compileKotlinJvm` 视环境）

Expected: BUILD SUCCESSFUL。

- [ ] **Step 2.6: Commit**

```bash
git add composeApp/build.gradle.kts composeApp/src/**/HttpClientFactory*.kt
git commit -m "feat(core): ktor engines per platform and serialization plugin"
```

---

### Task 3: `SecretsProvider` expect/actual + 测试假实现

**Files:**

- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/ports/SecretsProvider.kt`
- Create: `composeApp/src/androidMain/.../SecretsProvider.android.kt`
- Create: `composeApp/src/iosMain/.../SecretsProvider.ios.kt`
- Create: `composeApp/src/jvmMain/.../SecretsProvider.jvm.kt`
- Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/ports/FakeSecretsProvider.kt`

- [ ] **Step 3.1: common 接口与 expect**

`SecretsProvider.kt`:

```kotlin
package `fun`.abbas.android_res_translator.core.ports

fun interface SecretsProvider {
    operator fun get(key: String): String?
}

expect fun defaultSecretsProvider(): SecretsProvider
```

各端 `actual` 可先 `object : SecretsProvider { override fun get(key: String) = null }`，便于编译通过。

- [ ] **Step 3.2: Fake**

```kotlin
package `fun`.abbas.android_res_translator.core.ports

class FakeSecretsProvider(private val map: Map<String, String>) : SecretsProvider {
    override fun get(key: String): String? = map[key]
}
```

- [ ] **Step 3.3: commonTest 依赖**

在 [composeApp/build.gradle.kts](../../composeApp/build.gradle.kts) 的 `commonTest.dependencies { }` 中确保已有 `kotlin-test`，并增加：

```kotlin
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
```

（若 `libs.kotlinx.coroutines.test` 尚未在 `libs.versions.toml` 中定义，则在 Task 1 一并加入，版本与 `kotlinx-coroutines` 主版本对齐。）

- [ ] **Step 3.4: 写一条测试**

Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/ports/FakeSecretsProviderTest.kt`

```kotlin
package `fun`.abbas.android_res_translator.core.ports

import kotlin.test.Test
import kotlin.test.assertEquals

class FakeSecretsProviderTest {
    @Test
    fun returnsValue() {
        val s = FakeSecretsProvider(mapOf("baidu.appId" to "x"))
        assertEquals("x", s["baidu.appId"])
    }
}
```

Run: `.\gradlew.bat :composeApp:cleanTest :composeApp:testDebugUnitTest` 或 `:composeApp:iosSimulatorArm64Test` / `jvmTest` 中任一可用目标

Expected: 测试通过。

- [ ] **Step 3.5: Commit**

```bash
git add composeApp/src/**/SecretsProvider*.kt composeApp/src/commonTest/**/FakeSecretsProvider*.kt composeApp/build.gradle.kts
git commit -m "feat(core): SecretsProvider expect/actual and fake for tests"
```

---

### Task 4: 翻译领域模型与 `TranslationError`

**Files:**

- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/translation/TranslationModels.kt`

- [ ] **Step 4.1: 模型与结果密封类（禁止占用 `kotlin.Result` 名称）**

```kotlin
package `fun`.abbas.android_res_translator.core.translation

data class TranslationRequest(
    val sourceText: String,
    val sourceLanguage: String,
    val targetLanguage: String,
)

data class TranslationSuccess(
    val translatedText: String,
    val resolvedSourceLanguage: String,
    val resolvedTargetLanguage: String,
)

sealed class TranslationFailure {
    data class NoVendorForTarget(val targetLanguage: String) : TranslationFailure()
    data class VendorRejected(val vendorName: String, val detail: String) : TranslationFailure()
    data class NetworkFailure(val message: String) : TranslationFailure()
}

sealed class TranslationOutcome {
    data class Ok(val value: TranslationSuccess) : TranslationOutcome()
    data class Err(val failure: TranslationFailure) : TranslationOutcome()

    fun successOrNull(): TranslationSuccess? = (this as? Ok)?.value
    fun failureOrNull(): TranslationFailure? = (this as? Err)?.failure
}
```

- [ ] **Step 4.2: 测试**

Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/translation/TranslationModelsTest.kt`

```kotlin
package `fun`.abbas.android_res_translator.core.translation

import kotlin.test.Test
import kotlin.test.assertEquals

class TranslationModelsTest {
    @Test
    fun okWrapsValue() {
        val s = TranslationSuccess("hi", "zh", "en")
        val o: TranslationOutcome = TranslationOutcome.Ok(s)
        assertEquals("hi", o.successOrNull()!!.translatedText)
    }
}
```

Run: `.\gradlew.bat :composeApp:testDebugUnitTest`（或等价）

Expected: PASS

- [ ] **Step 4.3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/translation/TranslationModels.kt composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/translation/TranslationModelsTest.kt
git commit -m "feat(core): translation request/result models"
```

---

### Task 5: 厂商接口、`TranslationOrchestrator` 与链顺序

**Files:**

- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/translation/TranslationVendor.kt`
- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/translation/TranslationOrchestrator.kt`

- [ ] **Step 5.1: 定义厂商接口**

```kotlin
package `fun`.abbas.android_res_translator.core.translation

interface TranslationVendor {
    val name: String
    fun supportsTargetLanguage(isoOrAndroidCode: String): Boolean
    suspend fun translate(request: TranslationRequest): TranslationOutcome
}
```

- [ ] **Step 5.2: Orchestrator 按旧版顺序遍历**

顺序必须与 [TranslationManager.java](../../android_trans/src/main/java/org/example/translation/TranslationManager.java) 构造一致：**Huoshan → Lingvanex → Baidu → Youdao → Tencent**。

```kotlin
package `fun`.abbas.android_res_translator.core.translation

class TranslationOrchestrator(
    private val vendors: List<TranslationVendor>,
) {
    suspend fun translate(request: TranslationRequest): TranslationOutcome {
        var lastFailure: TranslationFailure? = null
        for (v in vendors) {
            if (!v.supportsTargetLanguage(request.targetLanguage)) continue
            when (val o = v.translate(request)) {
                is TranslationOutcome.Ok -> return o
                is TranslationOutcome.Err -> lastFailure = o.failure
            }
        }
        return TranslationOutcome.Err(
            lastFailure ?: TranslationFailure.NoVendorForTarget(request.targetLanguage),
        )
    }
}
```

- [ ] **Step 5.3: 双厂商 Mock 测试**

Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/translation/TranslationOrchestratorTest.kt`

```kotlin
package `fun`.abbas.android_res_translator.core.translation

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class StubVendor(
    override val name: String,
    private val supports: Boolean,
    private val outcome: TranslationOutcome,
) : TranslationVendor {
    override fun supportsTargetLanguage(isoOrAndroidCode: String): Boolean = supports
    override suspend fun translate(request: TranslationRequest): TranslationOutcome = outcome
}

class TranslationOrchestratorTest {
    @Test
    fun skipsUnsupportedThenUsesSecond() = runTest {
        val ok = TranslationOutcome.Ok(TranslationSuccess("X", "zh", "en"))
        val chain = TranslationOrchestrator(
            listOf(
                StubVendor("a", supports = false, outcome = TranslationOutcome.Err(TranslationFailure.VendorRejected("a", "skip"))),
                StubVendor("b", supports = true, outcome = ok),
            ),
        )
        val r = chain.translate(TranslationRequest("你好", "zh", "en"))
        assertEquals("X", r.successOrNull()!!.translatedText)
    }
}
```

Run: 同上 test 任务

Expected: PASS

- [ ] **Step 5.4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/translation/TranslationVendor.kt composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/translation/TranslationOrchestrator.kt composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/translation/TranslationOrchestratorTest.kt
git commit -m "feat(core): TranslationVendor and orchestrator chain"
```

---

### Task 6: `TranslatePlainTextUseCase`

**Files:**

- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/text/TranslatePlainTextUseCase.kt`
- Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/text/TranslatePlainTextUseCaseTest.kt`

- [ ] **Step 6.1: 用例类**

```kotlin
package `fun`.abbas.android_res_translator.core.text

import `fun`.abbas.android_res_translator.core.translation.TranslationOrchestrator
import `fun`.abbas.android_res_translator.core.translation.TranslationRequest

class TranslatePlainTextUseCase(
    private val orchestrator: TranslationOrchestrator,
) {
    suspend operator fun invoke(text: String, from: String, to: String) =
        orchestrator.translate(TranslationRequest(text, from, to))
}
```

- [ ] **Step 6.2: 测试**

```kotlin
package `fun`.abbas.android_res_translator.core.text

import `fun`.abbas.android_res_translator.core.translation.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TranslatePlainTextUseCaseTest {
    @Test
    fun delegates() = runTest {
        val vendor = object : TranslationVendor {
            override val name = "one"
            override fun supportsTargetLanguage(isoOrAndroidCode: String) = true
            override suspend fun translate(request: TranslationRequest) =
                TranslationOutcome.Ok(TranslationSuccess("Y", request.sourceLanguage, request.targetLanguage))
        }
        val uc = TranslatePlainTextUseCase(TranslationOrchestrator(listOf(vendor)))
        val r = uc("a", "zh", "en")
        assertEquals("Y", r.successOrNull()!!.translatedText)
    }
}
```

Run: test

Expected: PASS

- [ ] **Step 6.3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/text/TranslatePlainTextUseCase.kt composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/text/TranslatePlainTextUseCaseTest.kt
git commit -m "feat(core): TranslatePlainTextUseCase"
```

---

### Task 7: Baidu REST 厂商（首条真实 HTTP，可 MockEngine 测）

**Files:**

- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/translation/vendors/BaiduVendor.kt`

对照: [BaiduTranslation.java](../../android_trans/src/main/java/org/example/baidu_translation/BaiduTranslation.java) 中 URL `https://fanyi-api.baidu.com/api/trans/vip/translate`、表单字段 `q,from,to,appid,salt,sign`，`sign = MD5(appid+q+salt+secret)` 小写 32 位。

- [ ] **Step 7.1: 使用 `io.ktor.client.plugins.HttpSend` MockEngine 写失败与成功各一测**

Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/translation/vendors/BaiduVendorTest.kt`

```kotlin
package `fun`.abbas.android_res_translator.core.translation.vendors

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue
import `fun`.abbas.android_res_translator.core.ports.FakeSecretsProvider
import `fun`.abbas.android_res_translator.core.translation.TranslationRequest

class BaiduVendorTest {
    @Test
    fun parsesSuccessJson() = runTest {
        val engine = MockEngine {
            respond(
                content = """{"from":"zh","to":"en","trans_result":[{"src":"hi","dst":"HELLO"}]}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val vendor = BaiduVendor(
            client = client,
            secrets = FakeSecretsProvider(
                mapOf(
                    "baidu.appId" to "id",
                    "baidu.secretKey" to "sec",
                ),
            ),
        )
        val r = vendor.translate(TranslationRequest("hi", "zh", "en"))
        assertTrue(r.successOrNull()?.translatedText == "HELLO")
    }
}
```

- [ ] **Step 7.2: 实现 `BaiduVendor`**

实现要点：`submitForm` 或 `post` with `Parameters.build`；`salt` 用 `Random.nextInt()` 与旧版一致范围可自定；MD5 使用 `org.kotlincrypto.hash.md.Md5` 计算 digest 后转小写 hex（自行写 `fun ByteArray.toHexLower()`）。

（完整 `BaiduVendor.kt` 体较长，本计划要求：**在本任务同一 commit 内**写完可编译实现并通过 `BaiduVendorTest`。）

- [ ] **Step 7.3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/translation/vendors/BaiduVendor.kt composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/translation/vendors/BaiduVendorTest.kt
git commit -m "feat(core): Baidu translate REST vendor with MockEngine test"
```

---

### Task 8: Youdao 厂商

**Files:**

- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/translation/vendors/YoudaoVendor.kt`
- Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/translation/vendors/YoudaoVendorTest.kt`

对照: [YoudaoTranslation.java](../../android_trans/src/main/java/org/example/youdao_translation/YoudaoTranslation.java)（`appKey,salt,sign,curtime` 等字段以实际文件为准；**不要**在公共库中保留 `Thread.sleep(1000)` 作为业务逻辑，若需限流在 orchestrator 层统一实现）。

- [ ] **Step 8.1: MockEngine 返回成功 JSON 的测试**

- [ ] **Step 8.2: 实现 `YoudaoVendor`（SHA-256 签名与旧版字符串拼接规则一致）**

- [ ] **Step 8.3: Commit**

```bash
git commit -m "feat(core): Youdao translate REST vendor"
```

---

### Task 9: Lingvanex 厂商

**Files:**

- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/translation/vendors/LingvanexVendor.kt`
- Create: `composeApp/src/commonTest/kotlin/.../LingvanexVendorTest.kt`

对照: [LingvanexTranslation.java](../../android_trans/src/main/java/org/example/lingvanex_translation/LingvanexTranslation.java) URL `https://api-b2b.backenster.com/b1/api/v3/translate`，Header `Authorization: <token>`，Body JSON 字段与 [PostBean.java](../../android_trans/src/main/java/org/example/lingvanex_translation/PostBean.java)、[RespBean.java](../../android_trans/src/main/java/org/example/lingvanex_translation/RespBean.java) 对齐；语言表复制 `initSupportComparisonTable` 全表到 Kotlin `mapOf(...)`。

- [ ] **Step 9.1–9.3:** 测试 → 实现 → commit（message `feat(core): Lingvanex vendor`）

---

### Task 10: 火山 Huoshan REST 厂商

**Files:**

- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/translation/vendors/HuoshanVendor.kt`
- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/translation/vendors/VolcengineOpenApiSign.kt`（OpenAPI HMAC-SHA256 签名）
- Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/translation/vendors/HuoshanVendorTest.kt`

对照旧 SDK 调用语义 [HuoshanTranslation.java](../../android_trans/src/main/java/org/example/huoshan_translation/HuoshanTranslation.java)；在 **REST 文档** 中找到与 `translateText` 等价的 HTTP 接口（Volcengine 机器翻译 OpenAPI），用 Ktor 调用；密钥键名保持 `huoshan.accessKeyID` / `huoshan.secretAccessKey` 与旧 `config.properties` 一致。

- [x] **Step 10.1:** MockEngine 测试  
- [x] **Step 10.2:** 实现  
- [ ] **Step 10.3:** `git commit -m "feat(core): Huoshan translate REST vendor"`

---

### Task 11: 腾讯 TMT 厂商（API 3.0 TC3 签名）

**Files:**

- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/translation/vendors/TencentTc3Signer.kt`
- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/translation/vendors/TencentVendor.kt`
- Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/translation/vendors/TencentTc3SignerTest.kt`
- Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/translation/vendors/TencentVendorTest.kt`

对照: [TencentTranslation.java](../../android_trans/src/main/java/org/example/tencent_translation/TencentTranslation.java) 使用接口 `TextTranslate`；REST 等价于 POST `https://tmt.tencentcloudapi.com` Action `TextTranslate`，请求体 JSON 字段 `SourceText,Source,Target,ProjectId`。签名算法使用官方 **TC3-HMAC-SHA256**（文档编号以腾讯云「签名方法 v3」为准）。

- [x] **Step 11.1:** 先写 `TencentTc3Signer`，用已知官方示例向量在 `commonTest` 中断言 `Authorization` 前缀与 `Host`  
- [x] **Step 11.2:** `TencentVendor` 调通 MockEngine  
- [ ] **Step 11.3:** commit `feat(core): Tencent TMT vendor with TC3 signing`

---

### Task 12: 默认厂商列表装配与 spec 修订（D1 落地说明）

**Files:**

- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/translation/DefaultTranslationModule.kt`（组装 `HttpClient` + `SecretsProvider` + 五厂商 + `TranslationOrchestrator`）
- Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/translation/DefaultTranslationModuleTest.kt`
- Create: `composeApp/src/jvmTest/kotlin/fun/abbas/android_res_translator/core/translation/LiveVendorSmokeJvmTest.kt`（`-Dlive.vendor.smoke=true` + 环境变量时可选打腾讯实网）

- Modify: [docs/superpowers/specs/2026-05-14-android-trans-phase1-design.md](../specs/2026-05-14-android-trans-phase1-design.md) 修订记录增加 **0.4**：写明选定 `io.github.pdvrieze.xmlutil` 版本号与 Ktor 版本号

- [x] **Step 12.1:** `fun buildDefaultOrchestrator(secrets: SecretsProvider): TranslationOrchestrator` 按 **Huoshan→Lingvanex→Baidu→Youdao→Tencent** 实例化  
- [x] **Step 12.2:** 可选 `jvmTest`：`LiveVendorSmokeJvmTest` 在 `-Dlive.vendor.smoke=true` 且存在 `TENCENT_SECRET_ID`/`TENCENT_SECRET_KEY` 环境变量时走默认链打腾讯实网，否则跳过（D4）。  
- [ ] **Step 12.3:** commit `feat(core): wire default vendor chain and document xml/ktor versions`

---

### Task 13: `strings.xml` 内存模型与 xmlutil 往返

**Files:**

- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/model/StringResourceFile.kt`（含 `data class StringEntry(val name: String, val value: String)`、`StringArrayEntry` 等）
- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/xml/StringsXmlCodec.kt`
- Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/resources/xml/StringsXmlCodecTest.kt`

- [x] **Step 13.1: 最小 golden 输入**

测试用 XML：

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Demo</string>
    <string-array name="tabs">
        <item>A</item>
        <item>B</item>
    </string-array>
</resources>
```

断言：parse 后 `entries["app_name"]` 与 array 两条 item 可读；serialize 后再 parse **语义等价**（spec D2）。

- [x] **Step 13.2:** 实现 `StringsXmlCodec`（禁止依赖 dom4j）  
- [ ] **Step 13.3:** commit `feat(core): strings.xml codec with xmlutil`

---

### Task 14: `FilledTranslationConsumer` / `AllReplaceTranslationConsumer` 行为

**Files:**

- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/consumer/TranslationConsumer.kt`
- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/consumer/AbsTranslationConsumer.kt`
- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/consumer/FilledTranslationConsumer.kt`
- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/consumer/AllReplaceTranslationConsumer.kt`
- Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/resources/consumer/FilledTranslationConsumerTest.kt`

对照: [AbsTranslationConsumer.java](../../android_trans/src/main/java/org/example/translation/AbsTranslationConsumer.java)、[FilledTranslationConsumer.java](../../android_trans/src/main/java/org/example/translation/FilledTranslationConsumer.java)、[AllReplaceTranslationConsumer.java](../../android_trans/src/main/java/org/example/translation/AllReplaceTranslationConsumer.java)

- [x] **Step 14.1:** 定义 `suspend fun translateSegment(text: String, from: String, to: String): TranslationOutcome` 注入接口，便于测试替换为固定返回值  
- [x] **Step 14.2:** 用 Mock 翻译器验证：目标缺 key 时新增、存在时按策略更新；`string-array` 删除再重建逻辑与旧版一致（对照 Java 循环结构）  
- [ ] **Step 14.3:** commit `feat(core): resource translation consumers`

---

### Task 15: 单文件与整包用例 + `ResourceDiffAnalyzer`

**Files:**

- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/usecase/TranslateStringsXmlUseCase.kt`
- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/usecase/TranslateValuesFolderUseCase.kt`
- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/diff/ResourceDiffAnalyzer.kt`
- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/ports/FileTreePort.kt`（接口：`fun listChildren(dir: String): List<FileNode>` 等最小形态）
- Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/ports/InMemoryFileTree.kt` 假实现
- Create: `composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/usecase/ValuesFolderCountryTransformer.kt`（`DefaultValuesFolderCountryTransformer` + `isValuesResourceFolderName`）
- Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/resources/usecase/TranslateStringsXmlUseCaseTest.kt`
- Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/resources/usecase/TranslateValuesFolderUseCaseTest.kt`
- Create: `composeApp/src/commonTest/kotlin/fun/abbas/android_res_translator/core/resources/diff/ResourceDiffAnalyzerTest.kt`

对照 orchestration: [Dom4j.java](../../android_trans/src/main/java/org/example/dom4j/Dom4j.java) 中 `translationEntireValueFolder`、`translationXXtoXX`；**写盘必须可等待**（suspend 到完成或返回 `ByteArray` 由调用方写，避免旧版 fire-and-forget）。

- [x] **Step 15.1:** `TranslateStringsXmlUseCase`：读入 `String` XML → consumer → 输出 `String`  
- [x] **Step 15.2:** `TranslateValuesFolderUseCase`：通过 `FileTreePort` 枚举 `values*` 目录，按旧 `DefaultCountryTransformer` 语义解析文件夹名（Kotlin 化 [StringFolderCountryTransformer](../../android_trans/src/main/java/org/example/translation/StringFolderCountryTransformer.java) 等）  
- [x] **Step 15.3:** `ResourceDiffAnalyzer`：多语言多文件按 key 对齐，输出 `sealed class DiffRow`（`MissingInTarget`、`ExtraInTarget`、`ValueMismatch`）  
- [x] **Step 15.4:** commonTest golden 覆盖 Filled + AllReplace 各一例  
- [ ] **Step 15.5:** commit `feat(core): xml use cases and resource diff analyzer`

---

### Task 16: README 与 `config.properties.example`

**Files:**

- Modify: [README.md](../../README.md)
- Create: `config.properties.example`（仓库根；键名与旧 [config.properties](../../android_trans/config.properties) 对齐）

- [x] **Step 16.1:** 说明阶段一如何跑 `jvmTest`/`commonTest`、如何配置 `FakeSecretsProvider`、**切勿提交密钥**  
- [ ] **Step 16.2:** commit `docs: phase1 core config example and test instructions`

---

## Self-review（对照 spec）

| Spec 要求 | 覆盖任务 |
|-----------|----------|
| G1 通用翻译 | Task 5–7、8–12、6 |
| G2 res/XML 核心 | Task 13–15 |
| G3 对齐与差异 | Task 15 |
| G4 可验证 | 各 Task 中带 `commonTest` / MockEngine / golden |
| G5 UI 解耦 | Task 15 `FileTreePort`、全程无 Compose 依赖 |
| 方案 A（无 dom4j 终态） | Task 13 明确 xmlutil；旧 dom4j 不进入 `commonMain` |
| D4 CI 默认 Mock | Task 7+ MockEngine；Task 12 live 测试默认关闭 |
| D5 默认串行 | Consumer 内默认逐 key 串行；可选参数留到 Task 14 末尾 `maxParallelism: Int = 1` |

**Placeholder 自检:** 本计划未使用「TBD / 稍后实现」类措辞；厂商任务均指向具体旧文件路径与 URL；若某厂商 REST 与旧 Java 行为不一致，须在对应 Vendor 文件顶部 `KDoc` 记录差异原因。

---

## Execution handoff

**计划已保存至** `docs/superpowers/plans/2026-05-14-android-trans-phase1-implementation.md`。

**两种执行方式：**

1. **Subagent-Driven（推荐）** — 每个 Task 派生子代理执行，任务间人工复核，迭代快。  
2. **Inline Execution** — 本会话或单代理按 Task 顺序执行，配合 `executing-plans` 的检查点停顿。

**你希望采用哪一种？**（回复 `1` 或 `2` 即可；若开始写代码则视为退出纯计划模式。）
