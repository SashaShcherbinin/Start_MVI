plugins {
    id(libs.plugins.common.android.library.module)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "base.network"
}

dependencies {
    implementation(projects.base.domain)
    implementation(projects.base.logger)

    implementation(libs.bundles.compose)
    implementation(libs.bundles.network)
}
