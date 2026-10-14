plugins {
    alias(libs.plugins.scaffold.android.library.compose)
}

android {
    namespace = "com.example.globalarch.core.ui"
}

dependencies {
    api(projects.core.designsystem)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
