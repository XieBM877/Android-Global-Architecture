plugins {
    alias(libs.plugins.scaffold.android.library)
    alias(libs.plugins.scaffold.android.hilt)
}

android {
    namespace = "com.example.globalarch.core.datastore"
}

dependencies {
    api(projects.core.model)
    api(libs.androidx.datastore.preferences)
    api(libs.kotlinx.coroutines.core)
    implementation(projects.core.common)
}
