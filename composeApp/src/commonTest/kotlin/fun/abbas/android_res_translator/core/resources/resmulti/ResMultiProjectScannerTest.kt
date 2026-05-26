package `fun`.abbas.android_res_translator.core.resources.resmulti

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResMultiProjectScannerTest {

    @Test
    fun joinPath_normalizesSlash() {
        assertEquals("root/values/strings.xml", ResMultiProjectScanner.joinPath("root", "values/strings.xml"))
    }
}
