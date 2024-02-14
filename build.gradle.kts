import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
    `kotlin-dsl`
    idea
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.dokka)
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
    apply(plugin = "org.jetbrains.dokka")

    val javadocJar by tasks.registering(Jar::class) {
        archiveClassifier.set("javadoc")
    }

//    // configure only the HTML task
//    tasks.dokkaHtmlPartial {
//        outputDirectory.set(buildDir.resolve("docs/partial"))
//    }

    // configure all format tasks at once
    tasks.withType<DokkaTaskPartial>().configureEach {
        dokkaSourceSets.configureEach {
            includes.from("README.md")
        }
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/codemucker/codemucker-kotlin")
                credentials {
                    username =
                        project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
                }
            }
        }

//        publications {
//            register<MavenPublication>("gpr") {
//
//                artifact(javadocJar.get())
//                //TODO:
//                //       from(components["java"])0
//                pom {
//                    name.set("Codemucker Kotlin")
//                    description.set("Codemucker kotlin libraries")
//                    licenses {
//                        license {
//                            name.set("Apache")
//                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
//                        }
//                    }
//                    url.set("https://github.com/codemucker/codemucker-kotlin")
//                    issueManagement {
//                        system.set("Github")
//                        url.set("https://github.com/codemucker/codemucker-kotlin/issues")
//                    }
//                    scm {
//                        connection.set("https://github.com/codemucker/codemucker-kotlin.git")
//                        url.set("https://github.com/codemucker/codemucker-kotlin")
//                    }
//                    developers {
//                        developer {
//                            name.set("Bert van Brakel")
//                        }
//                    }
//                }
//            }
//        }
    }
}

