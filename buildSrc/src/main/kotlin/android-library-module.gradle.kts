plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    defaultConfig {
        compileSdk = project.projectLibs.versionInt("android.compileSdk")
        minSdk = project.projectLibs.versionInt("android.minSdk")
    }
    compileOptions {
        sourceCompatibility = projectLibs.javaVersion
        targetCompatibility = projectLibs.javaVersion
    }
    kotlinOptions {
        jvmTarget = projectLibs.javaVersion.toString()
    }
}
