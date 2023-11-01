import java.io.File
import java.io.FileInputStream
import java.lang.System.getenv
import java.util.*

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", "1.9.0"))
    }
}

plugins {
    kotlin("multiplatform") version "1.9.0"
    id("com.android.library")
    id("org.jetbrains.compose") version "1.5.1"
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
        jvmToolchain(17)
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    androidTarget {
        jvmToolchain(11)
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

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("com.outsidesource:oskit-kmp:4.0.0")
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                implementation("com.squareup.okio:okio:3.5.0")
                implementation("io.insert-koin:koin-core:3.4.3")
                implementation("org.jetbrains:markdown:0.5.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("io.ktor:ktor-client-core:2.3.3")
                implementation("io.ktor:ktor-client-cio:2.3.3")
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting
        val jvmTest by getting

        val androidMain by getting {
            dependencies {
                implementation("androidx.compose.foundation:foundation:1.5.2")
                implementation("androidx.compose.ui:ui:1.5.2")
                implementation("androidx.core:core-ktx:1.12.0")
                implementation("androidx.activity:activity-compose:1.7.2")
            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

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
