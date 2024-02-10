version = libs.versions.codemucker.klang

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.junit.jupiter.engine)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(libs.kotlinx.coroutines.core)

    api(libs.kotlinx.datetime)
    api(libs.kotlinx.serialization.json)
}
