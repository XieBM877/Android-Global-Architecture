plugins {
    alias(libs.plugins.scaffold.android.library)
}

android {
    namespace = "com.example.globalarch.core.mvi"
}

dependencies {
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.kotlinx.coroutines.core)

    testImplementation(projects.core.testing)
}
