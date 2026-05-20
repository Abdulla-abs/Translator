package `fun`.abbas.android_res_translator.ui.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
sealed interface RootRoute : NavKey {
    @Serializable
    data object Translate : RootRoute

    @Serializable
    data object Files : RootRoute

    @Serializable
    data object Settings : RootRoute

    @Serializable
    data object About : RootRoute
}

/** iOS / Desktop 等非 JVM 平台需显式注册 [NavKey] 多态序列化。 */
val rootNavSavedStateConfig =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(RootRoute.Translate::class, RootRoute.Translate.serializer())
                    subclass(RootRoute.Files::class, RootRoute.Files.serializer())
                    subclass(RootRoute.Settings::class, RootRoute.Settings.serializer())
                    subclass(RootRoute.About::class, RootRoute.About.serializer())
                }
            }
    }
