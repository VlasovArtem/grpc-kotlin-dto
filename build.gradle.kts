import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

plugins {
    java
    kotlin("jvm") version "1.5.31" apply false
    id("com.google.protobuf") version "0.8.15" apply false
    idea
}

ext["grpcVersion"] = "1.41.0"
ext["grpcKotlinVersion"] = "1.1.0"
ext["protobufVersion"] = "3.15.6"

subprojects {
    group = "org.avlasov"
    version = "1.0"

    apply {
        plugin("java")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("com.google.protobuf")
        plugin("maven-publish")
        plugin("signing")
        plugin("idea")
    }

    repositories {
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events = mutableSetOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
            showStandardStreams = true
        }
    }

    extensions.getByType<PublishingExtension>().publications {
        create<MavenPublication>("maven") {
            pom {
                url.set("https://github.com/VlasovArtem/grpc-kotlin-dto")

                scm {
                    connection.set("scm:git:https://github.com/VlasovArtem/grpc-kotlin-dto.git")
                    developerConnection.set("scm:git:git@github.com:VlasovArtem/grpc-kotlin-dto.git")
                    url.set("https://github.com/VlasovArtem/grpc-kotlin-dto")
                }

                licenses {
                    license {
                        name.set("Apache 2.0")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("avlasov.org")
                        name.set("Artem Vlasov")
                        email.set("vlasoartem21@gmail.com")
                    }
                }
            }
        }
    }

    extensions.getByType<PublishingExtension>().repositories {
        maven {
            val snapshotUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
            val releaseUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            url = if (version.safeAs<String>()?.endsWith("SNAPSHOT") == true) snapshotUrl else releaseUrl
            credentials {
                username = project.findProperty("sonatypeUsername")?.safeAs() ?: ""
                password = project.findProperty("sonatypePassword")?.safeAs() ?: ""
            }
        }
    }

    extensions.getByType<SigningExtension>().sign(extensions.getByType<PublishingExtension>().publications.named("maven").get())
}