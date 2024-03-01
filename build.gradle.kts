import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin

plugins {
    `kotlin-dsl`
    idea
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.completeKotlin) apply false
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


rootProject.plugins.withType<NodeJsRootPlugin> {
    rootProject.the<NodeJsRootExtension>().apply {
        nodeVersion = "20.11.1"
       // nodeDownloadBaseUrl = "https://nodejs.org/download/v8-canary"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask>().configureEach {
    args.add("--ignore-engines")
}





allprojects {

    if (findProperty("releaseVersion") != null) {
        project.version = project.findProperty("releaseVersion")
    } else {
        project.version =
            if (findProperty("version") == null) "0.0.1-SNAPSHOT" else "${version}-SNAPSHOT"
    }

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
                if ((project.version as String).endsWith("-SNAPSHOT")) {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/codemucker/codemucker-kotlin")
                    credentials {
                        username = project.findProperty("gpr.user") as String?
                            ?: System.getenv("GITHUB_ACTOR")
                        password = project.findProperty("gpr.key") as String?
                            ?: System.getenv("GITHUB_TOKEN")
                    }
                } else {
                    //TODO: sonatype, public maven repo
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/codemucker/codemucker-kotlin")
                    credentials {
                        username = project.findProperty("gpr.user") as String?
                            ?: System.getenv("GITHUB_ACTOR")
                        password = project.findProperty("gpr.key") as String?
                            ?: System.getenv("GITHUB_TOKEN")
                    }
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

