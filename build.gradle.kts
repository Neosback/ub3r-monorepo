import org.gradle.api.tasks.testing.Test

plugins {
    kotlin("jvm") version "1.9.10" apply false
}

subprojects {
    if (!path.startsWith(":skills:")) {
        apply(plugin = "application")
    }
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven(url = "https://repo.runelite.net")
        maven(url = "https://jitpack.io")
    }
}

val liveSkillModules = setOf(
    "agility", "cooking", "crafting", "farming", "firemaking", "fishing", "fletching", "herblore",
    "mining", "prayer", "runecrafting", "skillguide", "slayer", "smithing", "thieving", "woodcutting",
)

subprojects.filter { it.parent?.path == ":skills" && it.name in liveSkillModules }.forEach { skillProject ->
    skillProject.dependencies {
        add("testImplementation", "org.junit.jupiter:junit-jupiter-api:5.9.3")
        add("testRuntimeOnly", "org.junit.jupiter:junit-jupiter-engine:5.9.3")
    }
    skillProject.tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        doFirst {
            val hasKotlinTests = skillProject.file("src/test/kotlin").walkTopDown()
                .any { it.isFile && it.extension == "kt" }
            check(hasKotlinTests) {
                "${skillProject.path} has no live runtime test source; descriptor-only skill modules are not allowed."
            }
        }
    }
}

tasks.register("skillsCheck") {
    group = "verification"
    description = "Verifies the shared skill platform and every independently compiled skill module."
    dependsOn(
        ":skills:api:check", ":skills:runtime:check", ":skills:testkit:check",
        ":skills:agility:check", ":skills:cooking:check", ":skills:crafting:check",
        ":skills:farming:check", ":skills:firemaking:check", ":skills:fishing:check",
        ":skills:fletching:check", ":skills:herblore:check", ":skills:mining:check",
        ":skills:prayer:check", ":skills:runecrafting:check", ":skills:skillguide:check",
        ":skills:slayer:check", ":skills:smithing:check", ":skills:thieving:check",
        ":skills:woodcutting:check",
    )
}

tasks.register("verifySkillModuleDependencies") {
    group = "verification"
    description = "Rejects server dependencies from independently compiled skill modules and shared test fixtures."
    doLast {
        val offenders = file("skills").walkTopDown()
            .filter { it.name == "build.gradle.kts" }
            .filter { it.readText().contains("project(\":game-server\")") }
            .toList()
        check(offenders.isEmpty()) {
            "Skill modules must not depend on :game-server: ${offenders.joinToString { it.relativeTo(projectDir).path }}"
        }
    }
}

tasks.named("skillsCheck") { dependsOn("verifySkillModuleDependencies") }
