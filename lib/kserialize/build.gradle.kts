import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.androidLibrary) //<- Android Gradle Plugin for android target libraries
}

// only for developers
if (System.getenv("IS_CI") == null) {
    apply {
        plugin("com.louiscad.complete-kotlin")
    }
}

android {
    namespace = "org.codemucker.kserialize"

    compileSdk = 27
    compileSdkVersion = "android-27"

    defaultConfig {
        minSdk = 27
        compileSdk = 27
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.java.target.get().toInt())
        targetCompatibility = JavaVersion.toVersion(libs.versions.java.target.get().toInt())
    }
}

kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget()
    iosX64()
    js {
        browser()
        nodejs()
    }
    jvm()
    linuxX64()
    macosX64()
    mingwX64()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi(){
        nodejs()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":lib:klang"))
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.junit.jupiter.engine)
            }
        }
    }
    tasks.register("testClasses")
}

repositories {
    mavenCentral()
}