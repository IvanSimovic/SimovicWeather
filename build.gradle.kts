import io.gitlab.arturbosch.detekt.Detekt
import com.diffplug.gradle.spotless.SpotlessExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.spotless)
}

// DETEKT CONFIGURATION
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    val detektCheck = tasks.register<Detekt>("detektCheck") {
        description = "Checks that sourcecode satisfies detekt rules."
        autoCorrect = false
    }

    val detektApply = tasks.register<Detekt>("detektApply") {
        description = "Applies code formatting rules to sourcecode in-place."
        autoCorrect = true
    }

    listOf(detektCheck, detektApply).forEach { taskProvider ->
        taskProvider.configure {
            group = "verification"
            parallel = true
            ignoreFailures = false
            setSource(files(rootDir))
            config.setFrom(files("$rootDir/detekt.yml"))
            buildUponDefaultConfig = true
            include("**/*.kt", "**/*.kts")
            exclude("**/resources/**", "**/build/**", "**/generated/**")
            reports {
                html.required.set(true)
                xml.required.set(true)
            }
        }
    }
}

// SPOTLESS CONFIGURATION
subprojects {
    apply(plugin = "com.diffplug.spotless")

    configure<SpotlessExtension> {
        kotlin {
            target("**/*.kt", "**/*.kts")

            val customRuleSets = listOf(
                libs.ktlint.ruleset.standard,
                libs.nlopez.compose.rules,
                libs.twitter.compose.rules,
            ).map { it.get().toString() }

            ktlint().customRuleSets(customRuleSets)
            endWithNewline()
        }
        isEnforceCheck = false
    }
}
