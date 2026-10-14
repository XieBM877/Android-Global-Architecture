plugins {
    alias(libs.plugins.scaffold.jvm.library)
    alias(libs.plugins.scaffold.android.hilt)
}

dependencies {
    api(libs.kotlinx.coroutines.core)
}
