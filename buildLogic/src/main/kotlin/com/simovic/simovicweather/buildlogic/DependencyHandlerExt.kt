package com.simovic.simovicweather.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler

private const val IMPLEMENTATION = "implementation"
private const val DEBUG_IMPLEMENTATION = "debugImplementation"
private const val TEST_IMPLEMENTATION = "testImplementation"
private const val ANDROID_TEST_IMPLEMENTATION = "androidTestImplementation"

fun DependencyHandler.addSharedDependencies(project: Project) {
    with(project) {
        add(IMPLEMENTATION, libs.androidx.appcompat)
        add(IMPLEMENTATION, libs.androidx.core.ktx)
        add(IMPLEMENTATION, libs.material)

        add(IMPLEMENTATION, platform(libs.androidx.compose.bom))
        add(IMPLEMENTATION, libs.bundles.compose)
        add(DEBUG_IMPLEMENTATION, libs.androidx.compose.ui.tooling)

        add(TEST_IMPLEMENTATION, libs.junit)
        add(ANDROID_TEST_IMPLEMENTATION, libs.androidx.espresso.core)
        add(ANDROID_TEST_IMPLEMENTATION, libs.androidx.junit)
    }
}
