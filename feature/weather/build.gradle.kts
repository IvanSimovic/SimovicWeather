plugins {
    id("com.simovic.simovicweather.convention.feature")
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-parcelize")
}

android {
    namespace = "com.simovic.simovicweather.feature.weather"
}

dependencies {
    implementation(projects.feature.base)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.core)
    implementation(libs.coroutines.play.services)
    implementation(libs.play.services.location)
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.core)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.coroutines.test)
}
