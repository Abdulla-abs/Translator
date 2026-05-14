This is a Kotlin Multiplatform project targeting Android, iOS, Desktop (JVM).

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

### 阶段一核心（翻译 / `strings.xml`，无 UI）

- **运行测试**
  - Windows
    ```shell
    .\gradlew.bat :composeApp:jvmTest
    ```
  - macOS / Linux
    ```shell
    ./gradlew :composeApp:jvmTest
    ```
  会执行 `commonTest` 与 `jvmTest`（含 MockEngine 厂商用例、XML codec、Consumer、用例等）。仅编译可执行：
  ```shell
  ./gradlew :composeApp:compileKotlinJvm :composeApp:compileKotlinIosSimulatorArm64
  ```
  （Windows 将 `./gradlew` 换为 `.\gradlew.bat`。）
- **密钥与配置**：复制仓库根目录 [config.properties.example](./config.properties.example) 为 `config.properties` 并填入真实值；**切勿提交** `config.properties`（已在根 `.gitignore` 忽略）。键名与 [android_trans/config.properties](./android_trans/config.properties) 对齐。
- **测试中的密钥**：在 `composeApp` 的 `commonTest` 里使用 `FakeSecretsProvider(mapOf(...))` 注入假密钥，避免测试依赖本机 `config.properties`。
- **可选实网冒烟（JVM）**：仅本地调试使用 `-Dlive.vendor.smoke=true` 且配置环境变量 `TENCENT_SECRET_ID`、`TENCENT_SECRET_KEY`（详见 `composeApp/src/jvmTest/.../LiveVendorSmokeJvmTest.kt`）；CI 默认不触网。

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:run
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…