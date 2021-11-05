import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    application
}

application {
    mainClass.set("org.avlasov.kotlin.dto.generator.DtoGeneratorRunner")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("io.grpc:grpc-protobuf:${rootProject.ext["grpcVersion"]}")
    implementation("io.grpc:grpc-stub:${rootProject.ext["grpcVersion"]}")

    implementation("io.github.microutils:kotlin-logging:1.12.5")
    implementation("org.slf4j:slf4j-api:1.7.25")

    // Misc
    implementation(kotlin("reflect"))
    implementation("com.squareup:kotlinpoet:1.6.0")
    implementation("com.google.truth:truth:1.1")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("io.kotest:kotest-runner-junit5:5.0.0.M3")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")


    protobuf(files("src/main/resources/proto"))

    testProtobuf(files("src/test/resources/proto"))
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)

    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            pom {
                name.set("gRPC Kotlin DTO Generator")
                artifactId = "protoc-gen-dto-kotlin"
                description.set("gRPC Kotlin protoc DTO Generator plugin")
            }

            artifact(tasks.jar) {
                classifier = "jdk7"
            }
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${rootProject.ext["protobufVersion"]}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${rootProject.ext["grpcVersion"]}"
        }
        id("grpcdtokt") {
            path = tasks.jar.get().archiveFile.get().asFile.absolutePath
        }
    }
    generateProtoTasks {
        all().forEach {
            if (it.name.startsWith("generateTestProto")) {
                it.dependsOn("jar")
            }

            it.plugins {
                id("grpc")
                id("grpcdtokt") {
                    option("dto_suffix=Dto")
                    option("dto_package_suffix=dto")
                }
            }
        }
    }
}