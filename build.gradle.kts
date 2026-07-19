plugins {
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.compose.multiplatform).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.kmp.library).apply(false)
    alias(libs.plugins.ktlint).apply(false)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
}

val ktlintId = libs.plugins.ktlint.get().pluginId
val detektId = libs.plugins.detekt.get().pluginId
val koverId = libs.plugins.kover.get().pluginId

allprojects {
    apply(plugin = ktlintId)
    apply(plugin = detektId)
    apply(plugin = koverId)

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        filter {
            exclude { it.file.path.contains("build/") }
        }
    }

    detekt {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    }
}
