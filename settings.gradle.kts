pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io") // Для WebRTC
        maven("https://webrtc.github.io/webrtc-org/repositories") // Для WebRTC, если нужно
    }

    plugins {
        id("com.google.dagger.hilt.android") version "2.48"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") // Для WebRTC
        maven("https://webrtc.github.io/webrtc-org/repositories") // Для WebRTC
    }
}

rootProject.name = "ar_application"
include(":app")
include(":core")
include(":auth")

//include(":users")
//include(":videocall")
