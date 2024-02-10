plugins {
    `kotlin-dsl`
    idea
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.target.get().toInt()))
        //vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

kotlin {
    jvmToolchain(libs.versions.java.target.get().toInt())
}

idea {
    module {
        setDownloadSources(true)
        setDownloadSources(true)
    }
}

allprojects {
    group = "org.codemucker.kotlin"
    //version = '1.0'

    repositories {
        mavenCentral()
        //maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}


// from https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry
subprojects {
    apply(plugin = "maven-publish")
    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/codemucker/codemucker-kotlin")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
                }
            }
        }
        publications {
            register<MavenPublication>("gpr") {
                //TODO:
         //       from(components["java"])
            }
        }
    }
}

