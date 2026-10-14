plugins {
    alias(libs.plugins.scaffold.android.library)
    alias(libs.plugins.scaffold.android.hilt)
}

android {
    namespace = "com.example.globalarch.core.network"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        val apiBaseUrl = providers.gradleProperty("apiBaseUrl").getOrElse("")
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }
}

dependencies {
    api(projects.core.model)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)
    implementation(libs.timber)
    ksp(libs.moshi.kotlin.codegen)

    testImplementation(libs.okhttp.mockwebserver)
}
