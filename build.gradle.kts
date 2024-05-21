plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "1.9.24"
}

allprojects {
    repositories {
        // Use Maven Central for resolving dependencies.
        mavenCentral()
    }
}
