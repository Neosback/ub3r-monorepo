subprojects {
    apply(plugin = "application")
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven(url = "https://repo.runelite.net")
        maven(url = "https://jitpack.io")
    }
}
