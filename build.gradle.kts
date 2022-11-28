plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
}

allprojects {
    repositories {
        // Use Maven Central for resolving dependencies.
        mavenCentral()
    }
}
