import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serializationPlugin)
    alias(libs.plugins.secretGradlePlugin)
    alias(libs.plugins.buildKonfigPlugin)
}

fun localProperty(key: String): String {
    val file = rootProject.file("local.properties")
    if (!file.isFile) return ""
    val props = Properties()
    FileInputStream(file).use { props.load(it) }
    return props.getProperty(key) ?: ""
}

buildkonfig {
    packageName = "ru.kazan.itis.bikmukhametov.auth.impl"
    defaultConfigs {
        buildConfigField(
            type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            name = "YANDEX_CAPTCHA_SITE_KEY",
            value = localProperty("YANDEX_CAPTCHA_SITE_KEY")
        )

        buildConfigField(
            type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            name = "AUTH_BASE_URL",
            value = "https://auth.smartbotpro.ru"
        )

        buildConfigField(
            type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            name = "CABINET_DOMAIN",
            value = localProperty("CABINET_DOMAIN")
        )

        buildConfigField(
            type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            name = "BASE_URL",
            value = "https://${localProperty("CABINET_DOMAIN")}.smartbotpro.ru"
        )
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "FeatureLoginImpl"
            isStatic = true
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
            implementation(projects.shared.core.designsystem)
            implementation(projects.shared.core.network)
            implementation(projects.shared.feature.auth.api)

            implementation(libs.napier)

            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.components.resources)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.napier)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
        }
        androidMain.dependencies {
        }
    }
}

compose.resources {
    packageOfResClass = "ru.kazan.itis.bikmukhametov.impl.generated.resources"
}

android {
    namespace = "ru.kazan.itis.bikmukhametov.impl"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
