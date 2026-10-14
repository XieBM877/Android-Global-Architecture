import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("androidx.room")
            pluginManager.apply("com.google.devtools.ksp")
            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }
            val isApplication = pluginManager.hasPlugin("com.android.application")
            dependencies {
                add(if (isApplication) "implementation" else "api", libs.library("androidx-room-runtime"))
                add("ksp", libs.library("androidx-room-compiler"))
            }
        }
    }
}
