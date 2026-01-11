pluginManagement {
    // Had to move this here from build.gradle.kts because you can't use 'by project' or 'providers' inside the plugins {} block there. 
    // Gradle weirdness ¯\_(ツ)_/¯
    val kotlinVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
    }
}

rootProject.name = "kotlin-plugin-core"
