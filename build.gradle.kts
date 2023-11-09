import java.io.File
import java.io.FileInputStream
import java.lang.System.getenv
import java.util.*

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", "1.9.20"))
    }
}

plugins {
    kotlin("multiplatform") version "1.9.20"
    id("com.android.library")
    id("org.jetbrains.compose") version "1.5.10"
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.9.10"
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
        jvmToolchain(17)
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    androidTarget {
        jvmToolchain(17)
        publishLibraryVariants("release", "debug")
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "oskit-compose"
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("com.outsidesource:oskit-kmp:4.0.0")
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                implementation("com.squareup.okio:okio:3.5.0")
                implementation("io.insert-koin:koin-core:3.4.3")
                implementation("org.jetbrains:markdown:0.5.2")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("io.ktor:ktor-client-core:2.3.4")
                implementation("io.ktor:ktor-client-cio:2.3.4")
                api("org.jetbrains.kotlinx:atomicfu:0.21.0")
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.compose.foundation:foundation:1.5.4")
                implementation("androidx.compose.ui:ui:1.5.4")
                implementation("androidx.core:core-ktx:1.12.0")
                implementation("androidx.activity:activity-compose:1.8.0")
            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }

        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-ios:2.3.3")
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
    namespace = "com.outsidesource.oskitcompose"
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 24
        targetSdk = 34
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}
