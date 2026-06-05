pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        id("com.github.johnrengelman.shadow") version "8.1.1"
    }
}

rootProject.name = "ub3r"

include(":game-server")
include(":game-client")
include(":mystic-updatedclient")
include(":game-launcher")
include(":game-launcher-seamless")
