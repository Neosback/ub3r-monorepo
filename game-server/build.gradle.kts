import org.gradle.api.tasks.compile.JavaCompile

plugins {
    kotlin("jvm") version "1.9.10"
    id("application")
    `java-library`
}

application {
    mainClass.set("net.dodian.uber.game.Server")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(11)
}

val syncTestSourceSet = sourceSets.create("syncTest") {
    java.srcDirs("src/syncTest/java", "src/syncTest/kotlin")
    resources.srcDir("src/syncTest/resources")
    compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
    runtimeClasspath += output + compileClasspath
}

tasks.jar {

    manifest {
        attributes["Main-Class"] = "net.dodian.uber.game.Server"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    val sourcesMain = sourceSets.main.get()
    val contents = configurations.runtimeClasspath.get()
        .map { if (it.isDirectory) it else zipTree(it) } +
            sourcesMain.output

    from(contents)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")

    implementation("io.github.cdimascio:dotenv-kotlin:6.3.1")
    implementation("net.dv8tion:JDA:5.0.0-beta.24")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.4.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.0")
    implementation("org.apache.commons:commons-compress:1.21")

    implementation("mysql:mysql-connector-java:8.0.29")
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("org.mybatis:mybatis:3.5.10")

    implementation("com.google.code.gson:gson:2.7")

    implementation("io.netty:netty-all:4.1.108.Final")
    implementation("com.google.guava:guava:33.1.0-jre")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testImplementation("com.h2database:h2:2.2.224")

    implementation("com.sparkjava:spark-kotlin:1.0.0-alpha")

    implementation(kotlin("reflect"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

configurations[syncTestSourceSet.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[syncTestSourceSet.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

tasks.register<Test>("syncTest") {
    description = "Runs focused synchronization and transport verification tests"
    group = "verification"
    testClassesDirs = syncTestSourceSet.output.classesDirs
    classpath = syncTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

tasks.register("betaCheck") {
    group = "verification"
    description = "Comprehensive beta-readiness gate: unit tests + sync tests"

    dependsOn(":game-server:check")
    dependsOn(":game-server:syncTest")

    doLast {
        println("Beta readiness gate passed: unit tests and sync tests completed.")
    }
}

tasks.register<JavaExec>("runSyncBenchmark") {
    group = "verification"
    description = "Run the synchronization pipeline benchmark harness"
    classpath = syncTestSourceSet.runtimeClasspath
    mainClass.set("net.dodian.uber.game.runtime.sync.SyncPipelineBenchmark")
}

tasks.register<JavaExec>("dumpObjectDefs") {
    group = "tools"
    description = "Dump all object definitions from cache to JSON"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("net.dodian.uber.game.engine.systems.cache.ObjectDefinitionDumper")
    args = listOf("data/cache")
}

tasks.register<JavaExec>("dumpNpcDefs") {
    group = "tools"
    description = "Dump all NPC definitions from cache to JSON"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("net.dodian.uber.game.engine.systems.cache.NpcDefinitionDumper")
    args = listOf("data/cache")
}

tasks.register<JavaExec>("dumpSpotAnimDefs") {
    group = "tools"
    description = "Dump all spot animation (graphic) definitions from cache to JSON, optionally enriched with rsmod names"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("net.dodian.uber.game.engine.systems.cache.SpotAnimDefinitionDumper")
    args = listOf(
        "data/cache",
        "/Users/tylercovalt/Desktop/rsmod-main/.data/symbols/spotanim.sym",
    )
}

tasks.register<JavaExec>("dumpInterfaceDefs") {
    group = "tools"
    description = "Dump all interface/component definitions from cache to JSON"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("net.dodian.uber.game.engine.systems.cache.InterfaceDefinitionDumper")
    args = listOf("data/cache")
}

tasks.register<JavaExec>("generateRscm") {
    group = "tools"
    description = "Generate all RSCM mappings from JSON definitions to mappings/def/"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("net.dodian.uber.game.engine.systems.cache.RscmGenerator")
}
