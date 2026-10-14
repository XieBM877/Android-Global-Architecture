import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            tasks.withType<JavaCompile>().configureEach {
                options.release.set(17)
            }
            configureKotlinCompile()
            configureUnitTestDependencies()
            tasks.register("testDebugUnitTest") {
                group = "verification"
                description = "Runs JVM unit tests under the task name shared with Android modules."
                dependsOn("test")
            }
        }
    }
}
