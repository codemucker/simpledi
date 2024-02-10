version = libs.versions.codemucker.ksimpledi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.dokka)
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                api(kotlin("stdlib"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test.junit)
            }
        }
        val jvmMain by getting {
            dependencies {
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.junit.jupiter.engine)
            }
        }
    }
}
