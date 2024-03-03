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
    alias(libs.plugins.gradleRelease)
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

release {
    //default:failOnCommitNeeded.set(true)
    //default:failOnPublishNeeded.set(true)
    //default:failOnSnapshotDependencies.set(true)
    //toolchains.xml and settings.xml cause this to fail
    failOnUnversionedFiles.set(false)
    //default:failOnUpdateNeeded.set(true)
    //default:preTagCommitMessage.set("[Gradle Release Plugin] - pre tag commit: ")
    //default:tagCommitMessage.set("[Gradle Release Plugin] - creating tag: ")
    //default:newVersionCommitMessage.set("[Gradle Release Plugin] - new version commit: ")
    tagTemplate.set("v\${version}")
    versionPropertyFile.set("gradle.properties")
    //default:snapshotSuffix.set("-SNAPSHOT")
    //the branch we push a release to
    pushReleaseVersionBranch.set("release")

    buildTasks.add("clean")
    buildTasks.add("build")

    git {
        // the branch we branch from to create a new release
        requireBranch.set("release")
    }
}



rootProject.plugins.withType<NodeJsRootPlugin> {
    rootProject.the<NodeJsRootExtension>().apply {
        nodeVersion = "20.11.1"
        // nodeDownloadBaseUrl = "https://nodejs.org/download/v8-canary"
    }
}


tasks.withType<org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask>()
    .configureEach {
        args.add("--ignore-engines")
    }

// only want to publish after the release version has been set, but before it's updated to the next version
tasks.named("afterReleaseBuild") {
    finalizedBy("publish")
}

allprojects {
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

