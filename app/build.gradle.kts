plugins {
    id("com.simovic.simovicweather.convention.application")
    alias(libs.plugins.kotlin.serialization)
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

    buildTypes {
        release {
            optimization {
                enable = false
            }
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
