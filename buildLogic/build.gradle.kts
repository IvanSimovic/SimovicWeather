import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.simovic.simovicweather.buildlogic"

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(libs.versions.buildLogicJava.get())
    }

    jvmToolchain(
        libs.versions.buildLogicJava
            .get()
            .toInt(),
    )
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.spotless.gradlePlugin)

    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("applicationConvention") {
            id = "com.simovic.simovicweather.convention.application"
            implementationClass = "com.simovic.simovicweather.buildlogic.ApplicationConventionPlugin"
        }

        register("featureConvention") {
            id = "com.simovic.simovicweather.convention.feature"
            implementationClass = "com.simovic.simovicweather.buildlogic.FeatureConventionPlugin"
        }

        register("detektConvention") {
            id = "com.simovic.simovicweather.convention.detekt"
            implementationClass = "com.simovic.simovicweather.buildlogic.DetektConventionPlugin"
        }

        register("spotlessConvention") {
            id = "com.simovic.simovicweather.convention.spotless"
            implementationClass = "com.simovic.simovicweather.buildlogic.SpotlessConventionPlugin"
        }
    }
}
