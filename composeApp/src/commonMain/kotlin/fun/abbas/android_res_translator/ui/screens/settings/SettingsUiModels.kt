package `fun`.abbas.android_res_translator.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import `fun`.abbas.android_res_translator.ui.settings.AppSettingsSnapshot

enum class SettingsIconTint {
    Primary,
    Secondary,
    Tertiary,
}

data class SettingsFieldModel(
    val label: String,
    val placeholder: String,
    val isSecret: Boolean,
    val value: String,
    val onValueChange: (String) -> Unit,
)

data class SettingsProviderModel(
    val title: String,
    val icon: ImageVector,
    val iconTint: SettingsIconTint,
    val tag: String? = null,
    val spanFullWidth: Boolean = false,
    val fields: List<SettingsFieldModel>,
)

fun buildProviderSections(
    draft: AppSettingsSnapshot,
    onDraft: (AppSettingsSnapshot) -> Unit,
): List<SettingsProviderModel> =
    listOf(
        SettingsProviderModel(
            title = "Lingvanex",
            icon = Icons.Default.Language,
            iconTint = SettingsIconTint.Primary,
            fields =
                listOf(
                    SettingsFieldModel(
                        label = "API Token",
                        placeholder = "YOUR_TOKEN_OR_BEARER",
                        isSecret = true,
                        value = draft.lingvanexToken,
                        onValueChange = { onDraft(draft.copy(lingvanexToken = it)) },
                    ),
                ),
        ),
        SettingsProviderModel(
            title = "Tencent Cloud TMT",
            icon = Icons.Default.Cloud,
            iconTint = SettingsIconTint.Secondary,
            fields =
                listOf(
                    SettingsFieldModel(
                        label = "Secret ID",
                        placeholder = "YOUR_SECRET_ID",
                        isSecret = true,
                        value = draft.tencentSecretId,
                        onValueChange = { onDraft(draft.copy(tencentSecretId = it)) },
                    ),
                    SettingsFieldModel(
                        label = "Secret Key",
                        placeholder = "YOUR_SECRET_KEY",
                        isSecret = true,
                        value = draft.tencentSecretKey,
                        onValueChange = { onDraft(draft.copy(tencentSecretKey = it)) },
                    ),
                    SettingsFieldModel(
                        label = "Region",
                        placeholder = "ap-guangzhou",
                        isSecret = false,
                        value = draft.tencentRegion,
                        onValueChange = { onDraft(draft.copy(tencentRegion = it)) },
                    ),
                ),
        ),
        SettingsProviderModel(
            title = "Baidu Translate",
            icon = Icons.Default.Translate,
            iconTint = SettingsIconTint.Primary,
            fields =
                listOf(
                    SettingsFieldModel(
                        label = "App ID",
                        placeholder = "YOUR_APP_ID",
                        isSecret = false,
                        value = draft.baiduAppId,
                        onValueChange = { onDraft(draft.copy(baiduAppId = it)) },
                    ),
                    SettingsFieldModel(
                        label = "Secret Key",
                        placeholder = "YOUR_SECRET_KEY",
                        isSecret = true,
                        value = draft.baiduSecretKey,
                        onValueChange = { onDraft(draft.copy(baiduSecretKey = it)) },
                    ),
                ),
        ),
        SettingsProviderModel(
            title = "Youdao Translate",
            icon = Icons.Default.MenuBook,
            iconTint = SettingsIconTint.Tertiary,
            fields =
                listOf(
                    SettingsFieldModel(
                        label = "App ID",
                        placeholder = "YOUR_APP_ID",
                        isSecret = false,
                        value = draft.youdaoAppId,
                        onValueChange = { onDraft(draft.copy(youdaoAppId = it)) },
                    ),
                    SettingsFieldModel(
                        label = "Secret Key",
                        placeholder = "YOUR_SECRET_KEY",
                        isSecret = true,
                        value = draft.youdaoSecretKey,
                        onValueChange = { onDraft(draft.copy(youdaoSecretKey = it)) },
                    ),
                ),
        ),
        SettingsProviderModel(
            title = "Volcengine (Huoshan)",
            icon = Icons.Default.SmartToy,
            iconTint = SettingsIconTint.Tertiary,
            tag = "",
            spanFullWidth = true,
            fields =
                listOf(
                    SettingsFieldModel(
                        label = "Access Key ID",
                        placeholder = "YOUR_ACCESS_KEY_ID",
                        isSecret = false,
                        value = draft.huoshanAccessKeyId,
                        onValueChange = { onDraft(draft.copy(huoshanAccessKeyId = it)) },
                    ),
                    SettingsFieldModel(
                        label = "Secret Access Key",
                        placeholder = "YOUR_SECRET_ACCESS_KEY",
                        isSecret = true,
                        value = draft.huoshanSecretAccessKey,
                        onValueChange = { onDraft(draft.copy(huoshanSecretAccessKey = it)) },
                    ),
                ),
        ),
    )

fun SettingsIconTint.toColor(colors: androidx.compose.material3.ColorScheme): Color =
    when (this) {
        SettingsIconTint.Primary -> colors.primary
        SettingsIconTint.Secondary -> colors.secondary
        SettingsIconTint.Tertiary -> colors.tertiary
    }
