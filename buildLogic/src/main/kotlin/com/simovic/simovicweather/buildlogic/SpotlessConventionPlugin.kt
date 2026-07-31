package com.simovic.simovicweather.buildlogic

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class SpotlessConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.diffplug.spotless")

            extensions.configure<SpotlessExtension> {
                kotlin {
                    if (project == rootProject) {
                        target("buildLogic/src/**/*.kt")
                    } else {
                        target("**/*.kt")
                    }

                    val customRuleSets =
                        listOf(
                            libs.ktlint.ruleset.standard,
                            libs.nlopez.compose.rules,
                            libs.twitter.compose.rules,
                        ).map {
                            it.get().toString()
                        }

                    ktlint(
                        libs.versions.ktlint.ruleset.standard
                            .get(),
                    ).customRuleSets(customRuleSets)
                    endWithNewline()
                }

                kotlinGradle {
                    if (project == rootProject) {
                        target("*.gradle.kts", "buildLogic/**/*.gradle.kts")
                    } else {
                        target("**/*.gradle.kts")
                    }
                    ktlint(
                        libs.versions.ktlint.ruleset.standard
                            .get(),
                    )
                    endWithNewline()
                }

                isEnforceCheck = false
            }
        }
    }
}
