
plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.spring") version "1.6.0"
    jacoco
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}
