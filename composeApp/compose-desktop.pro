# Ktor ContentNegotiation discovers JSON support via Java ServiceLoader.
# Release packaging runs ProGuard per-jar; without these rules the provider class
# is stripped while META-INF/services still references it.
-keep class io.ktor.serialization.kotlinx.** { *; }
-keep class * implements io.ktor.serialization.kotlinx.KotlinxSerializationExtensionProvider { *; }

# Navigation 3 / NavigationEvent: SceneInfo extends NavigationEventInfo; ProGuard 7.4+
# optimizations can break NavDisplay bytecode (VerifyError: Bad type on operand stack).
-keep class androidx.navigation3.** { *; }
-keep class androidx.navigationevent.** { *; }

# ProGuard >= 7.4 can mis-optimize Compose / kotlinx bytecode on desktop (VerifyError).
-dontoptimize
