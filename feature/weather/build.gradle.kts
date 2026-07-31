plugins {
    id("com.simovic.simovicweather.convention.feature")
}

android {
    namespace = "com.simovic.simovicweather.feature.weather"
}

dependencies {
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}
