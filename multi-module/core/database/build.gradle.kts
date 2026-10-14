plugins {
    alias(libs.plugins.scaffold.android.library)
    alias(libs.plugins.scaffold.android.hilt)
    alias(libs.plugins.scaffold.android.room)
}

android {
    namespace = "com.example.globalarch.core.database"
}

dependencies {
    api(projects.core.model)
    api(libs.kotlinx.coroutines.core)
}
