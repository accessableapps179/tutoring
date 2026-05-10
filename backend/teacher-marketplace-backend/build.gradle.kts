val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.3.0"
    id("io.ktor.plugin") version "3.4.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor server dependencies
    implementation("io.ktor:ktor-server-core:3.4.0")
    implementation("io.ktor:ktor-server-content-negotiation:3.4.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.0")
    implementation("io.ktor:ktor-server-netty:3.4.0")
    implementation("io.ktor:ktor-server-config-yaml:3.4.0")
    implementation("io.ktor:ktor-server-websockets:3.4.0")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // Exposed ORM dependencies
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")

    // HikariCP
    implementation("com.zaxxer:HikariCP:5.0.1")

    // PostgreSQL
    implementation("org.postgresql:postgresql:42.7.3")

    // Password hashing
    implementation("org.mindrot:jbcrypt:0.4")

    // JWT
    implementation("com.auth0:java-jwt:4.4.0")

    // Ktor JWT Auth
    implementation("io.ktor:ktor-server-auth:3.4.0")
    implementation("io.ktor:ktor-server-auth-jwt:3.4.0")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Firebase Admin SDK
    implementation("com.google.firebase:firebase-admin:9.2.0")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host:3.4.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
}