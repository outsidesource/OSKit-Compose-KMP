import java.io.File
import java.io.FileInputStream
import java.lang.System.getenv
import java.util.*

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", "1.8.20"))
    }
}

plugins {
    kotlin("multiplatform") version "1.8.20"
    id("com.android.library")
    id("org.jetbrains.compose") version "1.4.0"
    id("maven-publish")
}

apply(from = "versioning.gradle.kts")

val versionProperty = Properties().apply {
    load(FileInputStream(File(rootProject.rootDir, "version.properties")))
}["version"] ?: "0.0.0"

group = "com.outsidesource"
version = versionProperty

repositories {
    mavenLocal()
    google()
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://plugins.gradle.org/m2/")
    maven {
        url = uri("https://maven.pkg.github.com/outsidesource/OSKit-KMP")
        credentials {
            val credentialProperties = Properties()
            if (getenv("OS_DEVELOPER") == null) {
                File(project.rootDir, "credential.properties").reader().use { stream -> credentialProperties.load(stream) }
            }

            username = getenv("OS_DEVELOPER") ?: credentialProperties["username"].toString()
            password = getenv("OS_TOKEN") ?: credentialProperties["password"].toString()
        }
    }
}

kotlin {
    jvm {
        jvmToolchain(11)
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    android {
        publishLibraryVariants("release", "debug")
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("com.outsidesource:oskit-kmp:3.1.2")
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api("androidx.compose.foundation:foundation:1.4.3")
                api("androidx.compose.ui:ui:1.4.3")
                api("androidx.core:core-ktx:1.10.1")
                api("androidx.activity:activity-compose:1.7.2")
                implementation("io.insert-koin:koin-core:3.4.0")
                implementation("org.jetbrains:markdown:0.2.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {}
        }
        val jvmTest by getting

        val androidMain by getting {
            dependencies {
                implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
    }

    afterEvaluate {
        getenv("GITHUB_REPOSITORY")?.let { repoName ->
            publishing {
                repositories {
                    maven {
                        name = "GitHubPackages"
                        url = uri("https://maven.pkg.github.com/$repoName")
                        credentials {
                            username = getenv("OS_DEVELOPER")
                            password = getenv("OS_TOKEN")
                        }
                    }
                }
            }
        }
    }
}

android {
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 24
        targetSdk = 33
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}
