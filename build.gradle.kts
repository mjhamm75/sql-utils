plugins {
    kotlin("multiplatform") version "1.6.10"
    id("maven-publish")
}

group = "me.jhamm"
version = "1.5-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation("org.springframework:spring-jdbc:5.3.18")
                implementation("com.google.code.gson:gson:2.9.0")

            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
