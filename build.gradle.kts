import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val javaVersion = "17"
val springBootVersion = "3.1.2"
val mockkVersion = "1.13.5"
val microutilsLoggingVersion = "3.0.5"
val assertjVersion = "3.24.2"
val springMockkVersion = "4.0.2"
val hibernateVersion = "6.2.7.Final"

plugins {
    val kotlinVersion = "1.9.0"

    idea
    application
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("io.spring.dependency-management") version "1.1.2"
    id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
    id("org.springframework.boot") version "3.1.2"
}

group = "com.github.egrmdev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
        mavenBom("org.testcontainers:testcontainers-bom:1.19.0")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // Logger
    implementation("io.github.microutils:kotlin-logging-jvm:$microutilsLoggingVersion")

    // DB
    runtimeOnly("org.liquibase:liquibase-core")
    implementation("org.hibernate.orm:hibernate-envers:$hibernateVersion")
    runtimeOnly("org.postgresql:postgresql")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // test
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("org.junit.vintage", "junit-vintage-engine")
    }
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")

    testImplementation("org.testcontainers:postgresql")
    testImplementation("io.mockk:mockk-jvm:$mockkVersion")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("com.ninja-squad:springmockk:$springMockkVersion")
}

tasks.test {
    useJUnitPlatform()

    // https://github.com/mockk/mockk/issues/681
    jvmArgs("--add-opens", "java.base/java.time=ALL-UNNAMED")

    reports {
        junitXml.required.set(true)
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = javaVersion
    }
}