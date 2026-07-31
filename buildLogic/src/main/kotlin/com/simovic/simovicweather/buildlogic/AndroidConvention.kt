package com.simovic.simovicweather.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion

fun ApplicationExtension.configureAndroid(projectVersions: org.gradle.accessors.dm.LibrariesForLibs.VersionAccessors) {
    compileSdk = projectVersions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = projectVersions.minSdk.get().toInt()
        targetSdk = projectVersions.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        val javaVersion = JavaVersion.toVersion(projectVersions.java.get())
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}

fun LibraryExtension.configureAndroid(projectVersions: org.gradle.accessors.dm.LibrariesForLibs.VersionAccessors) {
    compileSdk = projectVersions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = projectVersions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        val javaVersion = JavaVersion.toVersion(projectVersions.java.get())
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}
