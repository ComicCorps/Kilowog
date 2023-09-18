plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    application
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
    id("com.github.ben-manes.versions") version "0.48.0"
}

group = "github.buriedincode"
version = "0.1.1"

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
    implementation("org.jetbrains.kotlinx", "kotlinx-datetime", "0.4.1")
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.6.0")
    runtimeOnly("org.xerial", "sqlite-jdbc", "3.43.0.0")

    // Hoplite
    val hopliteVersion = "2.7.4"
    implementation("com.sksamuel.hoplite", "hoplite-core", hopliteVersion)
    implementation("com.sksamuel.hoplite", "hoplite-hocon", hopliteVersion)
    implementation("com.sksamuel.hoplite", "hoplite-json", hopliteVersion)
    implementation("com.sksamuel.hoplite", "hoplite-toml", hopliteVersion)
    implementation("com.sksamuel.hoplite", "hoplite-yaml", hopliteVersion)

    // XmlUtil
    val xmlutilVersion = "0.86.2"
    implementation("io.github.pdvrieze.xmlutil", "core-jvm", xmlutilVersion)
    implementation("io.github.pdvrieze.xmlutil", "serialization-jvm", xmlutilVersion)

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
    version.set("1.0.0")
}

tasks {
    val run by existing(JavaExec::class)
    run.configure {
        standardInput = System.`in`
    }
}
