import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.androidLibrary)
}

apply {
    from("$rootDir/gradle/include/android-library.gradle")
}

kotlin {
    jvmToolchain(libs.versions.java.target.get().toInt())

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
    // mingwX64()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }
//    @OptIn(ExperimentalWasmDsl::class)
//    wasmWasi() {
//        nodejs()
//    }


    sourceSets {
        commonMain {
            dependencies {
                api(kotlin("stdlib"))
                implementation(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.datetime)
                api(libs.kotlinx.serialization)
                api(libs.kotlinx.serialization.json)
                api(libs.uuid)
                api(libs.uri)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                // implementation(kotlin("stdlib-jdk8"))
            }
        }
        val jvmTest by getting {
            dependencies
                implementation(libs.junit.jupiter.engine)
            }
        }
    }
    tasks.register("testClasses")

    androidTarget {
        publishLibraryVariants("release", "debug")
    }
}