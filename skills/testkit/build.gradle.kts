plugins {
    kotlin("jvm")
    `java-library`
}

dependencies {
    api(project(":skills:api"))
    api(project(":skills:runtime"))
    // Expose the live-server API to consuming skill test projects. These
    // modules test the real registry; they do not start a separate server.
    api(project(":game-server"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
}

tasks.withType<Test> { useJUnitPlatform() }
