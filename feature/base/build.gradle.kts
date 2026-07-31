plugins {
    id("com.simovic.simovicweather.convention.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.simovic.simovicweather.feature.base"
}

dependencies {
    implementation(libs.kotlinx.serialization.core)
}
