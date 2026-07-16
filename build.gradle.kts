plugins {
    kotlin("jvm") version "1.9.10" apply false
}

subprojects {
    apply(plugin = "application")
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven(url = "https://repo.runelite.net")
        maven(url = "https://jitpack.io")
    }
}
