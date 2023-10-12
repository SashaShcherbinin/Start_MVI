plugins {
    id(libs.plugins.common.android.application.module)
    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.copmose)
}

object Version {
    private const val MAJOR = 1
    private const val MINOR = 0
    private const val PATCH = 0
    const val CODE = MAJOR * 10000 + MINOR * 100 + PATCH
    const val NAME = "$MAJOR.$MINOR.$PATCH"
}

android {
    namespace = "app"
    defaultConfig {
        applicationId = "start.mvi"
        versionCode = Version.CODE
        versionName = Version.NAME
    }
    buildTypes {
        named("release") {
            isMinifyEnabled = false
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            )
        }
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(projects.base.logger)
    implementation(projects.base.network)
    implementation(projects.base.storage)
    implementation(projects.base.compose)
    implementation(projects.base.domain)

    implementation(libs.bundles.koin.android)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.network)
    implementation(libs.coroutines.core)
    implementation(libs.image.coil)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.google.material)

    debugImplementation(libs.compose.tooling)
}