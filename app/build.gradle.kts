plugins {
    id("com.simovic.simovicweather.convention.application")
}

android {
    namespace = "com.simovic.simovicweather"

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
}
