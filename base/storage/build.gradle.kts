plugins {
    id(libs.plugins.common.kotlin.library.module)
}

dependencies {
    implementation(libs.coroutines.core)
    testImplementation(libs.bundles.test.common)
}
