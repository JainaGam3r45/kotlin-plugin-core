plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

val paperApiVersion: String by project

group = project.findProperty("group") as String
version = project.findProperty("version") as String
description = project.findProperty("description") as String

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")

    // Paper API bundles the kotlin stdlib, but IntelliJ complains if we don't declare it here for editing
    compileOnly(kotlin("stdlib"))
}

kotlin {
    jvmToolchain(21)
}

tasks {
    shadowJar {
        archiveClassifier.set("")
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        val props = mapOf(
            "version" to project.version,
            "description" to (project.description ?: "")
        )
        inputs.properties(props)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}
