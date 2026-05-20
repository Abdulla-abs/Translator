package `fun`.abbas.android_res_translator

import android.content.Context

object AndroidSettingsContext {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun require(): Context = appContext ?: error("AndroidSettingsContext.init 未调用")
}
