plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    application
    id("org.jlleitschuh.gradle.ktlint") version "11.5.0"
    id("com.github.ben-manes.versions") version "0.47.0"
}

group = "github.buriedincode"
version = "0.1.0"

println("Kilowog v$version")
println("Kotlin v${KotlinVersion.CURRENT}")
println("Java v${System.getProperty("java.version")}")
println("Arch: ${System.getProperty("os.arch")}")

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.github.junrar", "junrar", "7.5.5")
    implementation("org.jetbrains.kotlinx", "kotlinx-datetime", "0.4.0")
    runtimeOnly("org.xerial", "sqlite-jdbc", "3.42.0.0")

    // Hoplite
    val hopliteVersion = "2.7.4"
    implementation("com.sksamuel.hoplite", "hoplite-core", hopliteVersion)
    implementation("com.sksamuel.hoplite", "hoplite-yaml", hopliteVersion)

    // XmlUtils
    val xmlutilsVersion = "0.86.1"
    implementation("io.github.pdvrieze.xmlutil", "core-jvm", xmlutilsVersion)
    implementation("io.github.pdvrieze.xmlutil", "serialization-jvm", xmlutilsVersion)

    // Jackson
    val jacksonVersion = "2.15.2"
    implementation("com.fasterxml.jackson.core", "jackson-databind", jacksonVersion)
    implementation("com.fasterxml.jackson.dataformat", "jackson-dataformat-xml", jacksonVersion)
    implementation("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310", jacksonVersion)
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", jacksonVersion)

    // Log4j2
    implementation("org.apache.logging.log4j", "log4j-api-kotlin", "1.2.0")
    runtimeOnly("org.apache.logging.log4j", "log4j-slf4j2-impl", "2.20.0")
}

kotlin {
    jvmToolchain(17)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("github.buriedincode.kilowog.AppKt")
    applicationName = "Kilowog"
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("0.50.0")
}
