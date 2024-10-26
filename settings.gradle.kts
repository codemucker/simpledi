enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        //maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        // latest kotlin compose dev builds (not yet officially released)
        //maven("https://androidx.dev/storage/compose-compiler/repository/")
        google()
    }
}

plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    // NOTE: this has changed in gradle 8 and now requires some magic config to set the download urls
    // what these magic settings and values are is at this stage unknown. For now, it requires
    // that the java in the current path is whatever java.target is set in 'libs.versions.toml' or
    // greater (so locally installed)
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.6.0"
}

dependencyResolutionManagement {
    //repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        //maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    }
}

rootProject.name = "codemucker-kotlin"
include("lib:klang")
include("lib:kserialize")
include("lib:ksimpledi")
include("lib:kevent")

