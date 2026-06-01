rootProject.name = "KTS"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// Точки входа
include(":androidApp")
include(":iosApp")

// Зонтик объединяет main (навигация, App) + core + feature
include(":shared")
include(":shared:main")

include(":shared:core:designsystem")
include(":shared:core:network")
include(":shared:core:database")
include(":shared:core:analytics")

include(":shared:feature:onboarding")

include(":shared:feature:auth:api")
include(":shared:feature:auth:impl")

include(":shared:feature:main:api")
include(":shared:feature:main:impl")

include(":shared:feature:profile:api")
include(":shared:feature:profile:impl")

include(":shared:feature:chat:api")
include(":shared:feature:chat:impl")

include(":shared:feature:interlocutor-info:api")
include(":shared:feature:interlocutor-info:impl")

