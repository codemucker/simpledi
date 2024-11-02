import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.dokka)
    // alias(libs.plugins.androidLibrary)
}

apply {
    from("$rootDir/gradle/include/android-library.gradle")
}

kotlin {
    jvmToolchain(libs.versions.java.target.get().toInt())

    applyDefaultHierarchyTemplate()

    iosX64()
    js {
        browser()
        nodejs()
    }
    jvm()
    linuxX64()
    macosX64()
    //mingwX64()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }
//    @OptIn(ExperimentalWasmDsl::class)
//    wasmWasi {
//        nodejs()
//    }
    androidTarget()
    sourceSets {
        commonMain {
            dependencies {
                api(kotlin("stdlib"))
                api(projects.lib.klang)
                api(projects.lib.kserialize)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                api(projects.lib.ksimpledi)
            }
        }
        jvmMain {
            dependencies {
                //implementation(kotlin("stdlib-jdk8"))
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.junit.jupiter.engine)
            }
        }
//        val androidMain by getting {
//            dependencies {
//                //implementation(project(":shared"))
//            }
//        }
    }

    tasks.register("testClasses")
}
