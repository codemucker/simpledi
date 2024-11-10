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

    val os =
        org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem()

    applyDefaultHierarchyTemplate()
    androidTarget() {
        publishLibraryVariants("release", "debug")
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    js {
        browser()
        nodejs()
    }
    jvm()
    linuxX64()
    macosX64()
    if (os.isWindows) {
        mingwX64()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }
//    @OptIn(ExperimentalWasmDsl::class)
//    wasmWasi(){
//        nodejs()
//    }

    sourceSets {
        commonMain {
            dependencies {
                api(kotlin("stdlib"))
                api(projects.lib.klang)
                implementation(libs.kotlinx.coroutines.core)
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
            dependencies {
                implementation(libs.junit.jupiter.engine)
            }
        }
    }
    tasks.register("testClasses")
}
