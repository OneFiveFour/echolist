rootProject.name = "EchoList"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
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

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":app", ":apps:android", ":apps:desktop", ":apps:web")
include(":features:tasknotifications", ":features:browser", ":features:tasklist", ":features:note", ":features:login", ":features:tasksettings")
include(":core:designsystem", ":core:files", ":core:database", ":core:networking", ":core:protocol")
include(":core:session", ":core:tasks", ":core:recurrence", ":core:notifications")