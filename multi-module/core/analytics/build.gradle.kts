plugins {
    alias(libs.plugins.scaffold.android.library)
    alias(libs.plugins.scaffold.android.hilt)
}

android {
    namespace = "com.example.globalarch.core.analytics"
}

dependencies {
    implementation(libs.timber)
}
