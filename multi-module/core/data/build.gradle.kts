plugins {
    alias(libs.plugins.scaffold.android.library)
    alias(libs.plugins.scaffold.android.hilt)
}

android {
    namespace = "com.example.globalarch.core.data"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.domain)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.network)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(projects.core.testing)
}
