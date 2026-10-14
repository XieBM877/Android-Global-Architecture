plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.spotless)
}

val ktlintRules = mapOf(
    "ktlint_code_style" to "android_studio",
    "max_line_length" to "120",
    "ij_kotlin_allow_trailing_comma" to "true",
    "ij_kotlin_allow_trailing_comma_on_call_site" to "true",
    "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
    "ktlint_standard_filename" to "disabled",
    "ktlint_standard_property-naming" to "disabled",
    "ktlint_standard_class-signature" to "disabled",
    "ktlint_standard_function-signature" to "disabled",
    "ktlint_standard_multiline-expression-wrapping" to "disabled",
    "ktlint_standard_string-template-indent" to "disabled",
)

val formattedSourceDirectories = allprojects.map { it.projectDir.resolve("src") } +
    file("build-logic/convention/src")

val formattedScripts = allprojects.map { it.projectDir.resolve("build.gradle.kts") } +
    listOf(
        file("settings.gradle.kts"),
        file("build-logic/settings.gradle.kts"),
        file("build-logic/convention/build.gradle.kts"),
    )

spotless {
    kotlin {
        target(files(formattedSourceDirectories.filter { it.isDirectory }).asFileTree.matching { include("**/*.kt") })
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(ktlintRules)
    }
    kotlinGradle {
        target(files(formattedScripts.filter { it.isFile }))
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(ktlintRules)
    }
}
