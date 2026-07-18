import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

kotlin {
    android {
        namespace = "io.github.lucasfaiska.kmpdf.logic"
        compileSdk = 36
        compileSdkExtension = 19
        minSdk = 23

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.mock)
        }
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.pdf.viewer)
        }

        getByName("androidHostTest") {
            dependencies {
                implementation(libs.robolectric)
                implementation(libs.androidx.test.core)
                implementation(libs.androidx.test.junit)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
