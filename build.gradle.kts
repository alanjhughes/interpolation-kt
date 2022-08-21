import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.7.10"
    `maven-publish`
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
}

group = "com.alanhughes"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.alanhughes.interpolation"
            artifactId = "interpolation-kt"
            version = "0.1.0"

            from(components["java"])
        }
    }
}
