plugins {
    alias(libs.plugins.scaffold.jvm.library)
}

dependencies {
    api(projects.core.model)
    api(projects.core.domain)
    api(libs.kotlinx.coroutines.test)
    api(libs.junit)
    api(libs.turbine)
}
