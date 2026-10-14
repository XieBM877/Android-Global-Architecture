import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.intVersion(name: String): Int = findVersion(name).get().requiredVersion.toInt()

internal fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> = findLibrary(alias).get()

internal val Project.warningsAsErrors: Provider<Boolean>
    get() = providers.gradleProperty("warningsAsErrors").map(String::toBoolean).orElse(false)

internal fun Project.configureAndroidCommon(extension: CommonExtension) {
    extension.apply {
        compileSdk = libs.intVersion("androidCompileSdk")
        defaultConfig.minSdk = libs.intVersion("androidMinSdk")
        compileOptions.sourceCompatibility = JavaVersion.VERSION_17
        compileOptions.targetCompatibility = JavaVersion.VERSION_17
        lint.apply {
            abortOnError = true
            checkDependencies = true
            warningsAsErrors = this@configureAndroidCommon.warningsAsErrors.get()
            disable += setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion")
        }
        packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
        testOptions.unitTests.isReturnDefaultValues = true
    }
    configureKotlinCompile()
    configureUnitTestDependencies()
    configureInstrumentedTestDependencies()
}

internal fun Project.configureKotlinCompile() {
    val warningsAsErrors = warningsAsErrors
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            allWarningsAsErrors.set(warningsAsErrors)
        }
    }
}

internal fun Project.configureUnitTestDependencies() {
    dependencies {
        add("testImplementation", libs.library("junit"))
        add("testImplementation", libs.library("kotlinx-coroutines-test"))
        add("testImplementation", libs.library("turbine"))
        add("testImplementation", libs.library("mockk"))
    }
}

internal fun Project.configureInstrumentedTestDependencies() {
    dependencies {
        add("androidTestImplementation", libs.library("androidx-test-ext-junit"))
        add("androidTestImplementation", libs.library("androidx-test-runner"))
        add("androidTestImplementation", libs.library("androidx-test-core"))
        add("androidTestImplementation", libs.library("androidx-test-espresso-core"))
        add("androidTestImplementation", libs.library("kotlinx-coroutines-test"))
    }
}
