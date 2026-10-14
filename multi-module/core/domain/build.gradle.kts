plugins {
    alias(libs.plugins.scaffold.jvm.library)
}

dependencies {
    api(projects.core.model)
    api(libs.kotlinx.coroutines.core)
    implementation(projects.core.common)
    implementation(libs.javax.inject)

    testImplementation(projects.core.testing)
}
