import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    //alias(libs.plugins.koinCompilerPlugin)
    //alias(libs.plugins.buildKonfigPlugin)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    val iosArm64 = iosArm64()
    val iosSimulatorArm64 = iosSimulatorArm64()

    listOf(iosArm64, iosSimulatorArm64).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = false
            export(projects.shared.main)
            export(libs.ktor.client.darwin)
        }
    }

    sourceSets {
        val iosMain by creating {
            dependsOn(commonMain.get())
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }

        commonMain.dependencies {

            api(projects.shared.main)

            api(projects.shared.core.designsystem)
            api(projects.shared.core.network)

            api(projects.shared.feature.onboarding)
            api(projects.shared.feature.auth.impl)
            api(projects.shared.feature.main.impl)
        }

        iosMain {
            dependsOn(commonMain.get())
        }
    }
}

compose.resources {
    packageOfResClass = "ru.kazan.itis.bikmukhametov.designsystem.generated.resources"
}

android {
    namespace = "ru.kazan.itis.bikmukhametov.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}