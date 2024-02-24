import org.jetbrains.kotlin.commonizer.OptimisticNumberCommonizationEnabledKey.alias

version = libs.versions.codemucker.klang

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.dokka)
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
    iosX64()
//    @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
//    wasmJs{
//        browser()
//        nodejs()
//    }
    mingwX64()


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