import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.androidLibrary) //<- Android Gradle Plugin for android target libraries
    //alias(libs.plugins.androidApplication)  //<- Android Gradle Plugin for applications
    //alias(libs.plugins.kotlin.android)
}

android {
    namespace = "org.codemucker.kevent"

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

// only for developers
if (System.getenv("IS_CI") == null) {
    apply {
        plugin("com.louiscad.complete-kotlin")
    }
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
    mingwX64()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }
    androidTarget("main") {


//        @OptIn(ExperimentalKotlinGradlePluginApi::class)
//        compilerOptions {
//            jvmTarget.set(JvmTarget.JVM_11)
//        }
//
//        compilations.all {
//
//        }
    }
    sourceSets {
        commonMain {
            dependencies {
                api(kotlin("stdlib"))
                api(project(":lib:klang"))
                api(project(":lib:kserialize"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":lib:ksimpledi"))
            }
        }
        jvmMain {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
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



repositories {
    mavenCentral()
}
