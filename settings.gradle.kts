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
include(":routefinder")

include(
    ":skills:api",
    ":skills:runtime",
    ":skills:testkit",
    ":skills:agility",
    ":skills:cooking",
    ":skills:crafting",
    ":skills:farming",
    ":skills:firemaking",
    ":skills:fishing",
    ":skills:fletching",
    ":skills:herblore",
    ":skills:mining",
    ":skills:prayer",
    ":skills:runecrafting",
    ":skills:skillguide",
    ":skills:slayer",
    ":skills:smithing",
    ":skills:thieving",
    ":skills:woodcutting",
)
