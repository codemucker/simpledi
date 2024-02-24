version = libs.versions.codemucker.ksimpledi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.dokka)
    //alias(libs.plugins.androidLibrary) //<- Android Gradle Plugin for android target libraries
    //alias(libs.plugins.androidApplication)  //<- Android Gradle Plugin for applications
}

if (!"ci".equals(project.properties["build.profile"])) {
    apply {
        plugin("com.louiscad.complete-kotlin")
    }
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvm()
    js {
        browser()
        nodejs()
    }
    linuxX64()
    //androidTarget()
    mingwX64()

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
}

repositories {
    mavenCentral()
}
