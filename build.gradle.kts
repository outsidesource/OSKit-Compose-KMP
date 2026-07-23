@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.*

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.dokka)
    id("maven-publish")
    id("com.vanniktech.maven.publish") version "0.28.0"
}

apply(from = "versioning.gradle.kts")

val versionProperty = Properties().apply {
    load(FileInputStream(File(rootProject.rootDir, "version.properties")))
}["version"] ?: "0.0.0"

group = "com.outsidesource"
version = versionProperty

dependencies { androidRuntimeClasspath(libs.compose.ui.tooling) }

kotlin {
    jvmToolchain(17)

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        freeCompilerArgs.add("-Xconsistent-data-class-copy-visibility")
    }

    androidLibrary {
        namespace = "com.outsidesource.oskitcompose"
        compileSdk { version = release(libs.versions.android.compileSdk.get().toInt()) }
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        androidResources { enable = true }

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            execution = "HOST"
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        macosArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "oskit-compose"
        }
    }

    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            binaries.executable()
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.oskit.kmp)
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.ui.preview)
                implementation(libs.compose.resources)
                implementation(libs.okio)
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.markdown)
                implementation(libs.kotlinx.datetime)
                implementation(libs.ktor.client.core)
                implementation(libs.kotlinx.atomicfu)
                implementation(libs.material.icons)
                implementation(libs.navigationEvent)
                implementation(libs.navigationEvent.compose)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.activity.compose)
                implementation(libs.lifecycle.process)
                implementation(libs.core.ktx)
                implementation(libs.ktor.client.cio)
            }
        }

        val androidDeviceTest by getting {
            dependencies {
                implementation(libs.junit)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }

        val iosMain by getting {
            dependencies {
                implementation(libs.ktor.client.ios)
            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
                implementation(libs.kotlinx.browser)
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()

    configure(
        platform = KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("dokkaGenerateHtml"),
            sourcesJar = true,
            androidVariantsToPublish = listOf("debug", "release"),
        )
    )

    pom {
        description.set("An opinionated architecture/library for Compose Multiplatform development")
        name.set(project.name)
        url.set("https://github.com/outsidesource/OSKit-Compose-KMP")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://spdx.org/licenses/MIT.html")
                distribution.set("https://spdx.org/licenses/MIT.html")
            }
        }
        scm {
            url.set("https://github.com/outsidesource/OSKit-Compose-KMP")
            connection.set("scm:git:git://github.com/outsidesource/OSKit-Compose-KMP.git")
            developerConnection.set("scm:git:ssh://git@github.com/outsidesource/OSKit-Compose-KMP.git")
        }
        developers {
            developer {
                id.set("ryanmitchener")
                name.set("Ryan Mitchener")
            }
            developer {
                id.set("osddeveloper")
                name.set("Outside Source")
            }
        }
    }
}

// To get compose compiler metrics run: ./gradlew publishToMavenLocal -PcomposeCompilerReports=true
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlin {
        compilerOptions {
            if (project.findProperty("composeCompilerReports") == "true") {
                freeCompilerArgs.add("-P plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${projectDir.resolve("/build").absolutePath}/compose_compiler")
            }
            if (project.findProperty("composeCompilerMetrics") == "true") {
                freeCompilerArgs.add("-P plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${projectDir.resolve("/build").absolutePath}/compose_compiler")
            }
        }
    }
}