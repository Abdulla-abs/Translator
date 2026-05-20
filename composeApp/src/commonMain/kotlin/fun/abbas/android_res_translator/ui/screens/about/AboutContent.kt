package `fun`.abbas.android_res_translator.ui.screens.about

data class OpenSourceLicense(
    val name: String,
    val license: String,
    val description: String,
)

data class ContributorOrg(
    val handle: String,
    val tag: String,
)

object AboutContent {
    const val GITHUB_URL = "https://github.com/abbas/android-res-translator"
    const val KOTLIN_VERSION_LABEL = "Kotlin 2.3"

    val featuredLicenses =
        listOf(
            OpenSourceLicense(
                name = "Ktor",
                license = "Apache 2.0",
                description = "Async HTTP framework for Kotlin platforms.",
            ),
            OpenSourceLicense(
                name = "SQLDelight",
                license = "Apache 2.0",
                description = "Type-safe SQL query generation for KMP.",
            ),
            OpenSourceLicense(
                name = "Compose",
                license = "Apache 2.0",
                description = "Jetpack Compose for Desktop and Multiplatform.",
            ),
            OpenSourceLicense(
                name = "xmlutil",
                license = "Apache 2.0",
                description = "Multiplatform XML serialization for Android resources.",
            ),
        )

    val allLicenses: List<OpenSourceLicense> =
        featuredLicenses +
            listOf(
                OpenSourceLicense("Kotlin", "Apache 2.0", "Modern programming language for multiplatform development."),
                OpenSourceLicense("kotlinx.serialization", "Apache 2.0", "Kotlin multiplatform serialization library."),
                OpenSourceLicense("Navigation3", "Apache 2.0", "Compose navigation library."),
                OpenSourceLicense("Material 3", "Apache 2.0", "Material Design components for Compose."),
            )

    val contributorOrgs =
        listOf(
            ContributorOrg("@jetbrains", "Core Frameworks"),
            ContributorOrg("@square", "Data Persistence"),
            ContributorOrg("@cashapp", "Platform Bridging"),
            ContributorOrg("@google", "Material Design"),
        )

    val extraContributorCount = 12
}
