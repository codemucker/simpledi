plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.dokka)
}

// only for developers
if (System.getenv("IS_CI") == null) {
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
//    @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
//    wasmJs{
//        browser()
//        nodejs()
//    }
    linuxX64()
    macosX64()
    mingwX64()
    iosX64()

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