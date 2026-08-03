import java.util.Properties

plugins {
    id("com.simovic.simovicweather.convention.application")
    alias(libs.plugins.kotlin.serialization)
}

val releaseSigningPropertiesFile = rootProject.file("keystore.properties")
val releaseSigningProperties =
    releaseSigningPropertiesFile
        .takeIf { it.isFile }
        ?.let { file ->
            Properties().apply {
                file.inputStream().use(::load)
            }
        }

android {
    namespace = "com.simovic.simovicweather"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.simovic.simovicweather"
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        releaseSigningProperties?.let { properties ->
            create("release") {
                storeFile = rootProject.file(properties.getProperty("storeFile"))
                storePassword = properties.getProperty("storePassword")
                keyAlias = properties.getProperty("keyAlias")
                keyPassword = properties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            signingConfig = signingConfigs.findByName("release")
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
}

dependencies {
    implementation(projects.feature.base)
    implementation(projects.feature.weather)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.bundles.networking)
    implementation(libs.androidx.core.splashscreen)
}
