import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.androidLibrary) //<- Android Gradle Plugin for android target libraries
    //alias(libs.plugins.androidApplication)  //<- Android Gradle Plugin for applications
}

apply{
    from("$rootDir/gradle/include/android-library.gradle")
}

// only for developers
if (System.getenv("IS_CI") == null) {
    apply {
        plugin("com.louiscad.complete-kotlin")
    }
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
    mingwX64()
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
                api(project(":lib:klang"))
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
