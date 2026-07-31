package com.simovic.simovicweather.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class ApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
            pluginManager.apply("com.simovic.simovicweather.convention.spotless")

            extensions.configure<ApplicationExtension> {
                configureAndroid(versions)
            }

            dependencies {
                addSharedDependencies(project)
            }
        }
    }
}
