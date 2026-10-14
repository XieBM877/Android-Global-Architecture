package com.example.globalarch.architecture

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageBoundaryTest {
    @Test
    fun productionSources_respectPackageBoundaries() {
        val files = File(SOURCE_ROOT).walkTopDown().filter { it.extension == "kt" }.toList()
        assertTrue("no production sources found under $SOURCE_ROOT", files.size >= MIN_EXPECTED_FILES)

        val violations = files.flatMap { violationsIn(it.path, it.readText()) }

        assertTrue(violations.joinToString(separator = "\n"), violations.isEmpty())
    }

    @Test
    fun featureImportingDataLayer_isReported() {
        val source = sourceOf("feature.home", "core.data.OfflineFirstNotesRepository")

        assertEquals(1, violationsIn("Home.kt", source).size)
    }

    @Test
    fun featureImportingAnotherFeature_isReported() {
        val source = sourceOf("feature.home", "feature.detail.DetailViewModel")

        assertEquals(1, violationsIn("Home.kt", source).size)
    }

    @Test
    fun uiImportingDomain_isReported() {
        val source = sourceOf("core.ui", "core.domain.repository.NotesRepository")

        assertEquals(1, violationsIn("Ui.kt", source).size)
    }

    @Test
    fun domainImportingAndroid_isReported() {
        val source = """
            package $BASE_PACKAGE.core.domain

            import android.content.Context
        """.trimIndent()

        assertEquals(1, violationsIn("Domain.kt", source).size)
    }

    @Test
    fun featureImportingSharedLayers_isAllowed() {
        val source = sourceOf("feature.home", "core.domain.repository.NotesRepository")

        assertTrue(violationsIn("Home.kt", source).isEmpty())
    }

    @Test
    fun appPackage_mayImportAnyLayer() {
        val source = """
            package $BASE_PACKAGE.navigation

            import $BASE_PACKAGE.core.data.DataModule
            import $BASE_PACKAGE.feature.home.HomeRoute
        """.trimIndent()

        assertTrue(violationsIn("App.kt", source).isEmpty())
    }

    @Test
    fun undeclaredPackage_isReported() {
        val source = sourceOf("core.unknown", "core.model.Note")

        assertEquals(1, violationsIn("Unknown.kt", source).size)
    }

    private fun sourceOf(ownerPackage: String, importedType: String): String = """
        package $BASE_PACKAGE.$ownerPackage

        import $BASE_PACKAGE.$importedType
    """.trimIndent()

    private fun violationsIn(path: String, source: String): List<String> {
        val owner = moduleOf(PACKAGE_REGEX.find(source)?.groupValues?.get(1).orEmpty()) ?: return emptyList()
        val allowed = ALLOWED_DEPENDENCIES[owner]
            ?: return listOf("$path: $owner is not declared in ALLOWED_DEPENDENCIES")
        val imports = IMPORT_REGEX.findAll(source).map { it.groupValues[1] }.toList()

        val layering = imports
            .filter { imported -> moduleOf(imported)?.let { it != owner && it !in allowed } == true }
            .map { imported -> "$path: $owner must not import $imported" }
        val platform = imports
            .filter { imported -> owner in PURE_KOTLIN_MODULES && ANDROID_PREFIXES.any { imported.startsWith(it) } }
            .map { imported -> "$path: $owner must not import $imported" }
        return layering + platform
    }

    private fun moduleOf(name: String): String? =
        MODULE_REGEX.find(name)?.let { "${it.groupValues[1]}.${it.groupValues[2]}" }

    private companion object {
        const val BASE_PACKAGE = "com.example.globalarch"
        const val SOURCE_ROOT = "src/main/kotlin"
        const val MIN_EXPECTED_FILES = 20

        val MODULE_REGEX = Regex("^${Regex.escape(BASE_PACKAGE)}\\.(core|feature)\\.(\\w+)")
        val PACKAGE_REGEX = Regex("^package\\s+([\\w.]+)", RegexOption.MULTILINE)
        val IMPORT_REGEX = Regex("^import\\s+([\\w.]+)", RegexOption.MULTILINE)
        val ANDROID_PREFIXES = listOf("android.", "androidx.")

        val PURE_KOTLIN_MODULES = setOf("core.model", "core.common", "core.domain")

        val FEATURE_ALLOWED = setOf(
            "core.mvi",
            "core.ui",
            "core.designsystem",
            "core.domain",
            "core.model",
            "core.analytics",
            "core.common",
        )

        val ALLOWED_DEPENDENCIES: Map<String, Set<String>> = mapOf(
            "core.model" to emptySet(),
            "core.common" to emptySet(),
            "core.domain" to setOf("core.model", "core.common"),
            "core.mvi" to emptySet(),
            "core.analytics" to emptySet(),
            "core.designsystem" to emptySet(),
            "core.ui" to setOf("core.designsystem"),
            "core.database" to setOf("core.model", "core.common"),
            "core.datastore" to setOf("core.model", "core.common"),
            "core.network" to setOf("core.model", "core.common"),
            "core.data" to setOf(
                "core.model",
                "core.common",
                "core.domain",
                "core.database",
                "core.datastore",
                "core.network",
            ),
            "feature.home" to FEATURE_ALLOWED,
            "feature.detail" to FEATURE_ALLOWED,
            "feature.settings" to FEATURE_ALLOWED,
        )
    }
}
