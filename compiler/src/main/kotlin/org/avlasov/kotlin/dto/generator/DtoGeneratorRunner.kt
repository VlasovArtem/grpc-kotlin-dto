package org.avlasov.kotlin.dto.generator

import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Throwables
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.Descriptors
import com.google.protobuf.compiler.PluginProtos
import com.squareup.kotlinpoet.FileSpec
import org.avlasov.kotlin.dto.generator.utils.capitalizeString
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import java.nio.file.Paths

object DtoGeneratorRunner {

    @JvmStatic
    fun main(args: Array<String>) {
        mainAsProtocPlugin(System.`in`, System.out)
    }

    @VisibleForTesting
    fun mainAsProtocPlugin(input: InputStream, output: OutputStream) {
        val generatorRequest = try {
            input.buffered().use {
                PluginProtos.CodeGeneratorRequest.parseFrom(it)
            }
        } catch (failure: Exception) {
            throw IOException(
                """
        Attempted to run proto extension generator as protoc plugin, but could not read
        CodeGeneratorRequest.
        """.trimIndent(),
                failure
            )
        }

        val dtoGenerator = DtoGenerator(prepareDtoGeneratorConfiguration(generatorRequest.parameter))

        output.buffered().use {
            val descriptorsByName = mutableMapOf<ProtoFileName, Descriptors.FileDescriptor>()

            generatorRequest.protoFileList.forEach { protoFile ->
                val dependencies = protoFile.dependencyNames.map(descriptorsByName::getValue)

                val fileDescriptor = Descriptors.FileDescriptor.buildFrom(protoFile, dependencies.toTypedArray())

                descriptorsByName[protoFile.fileName] = fileDescriptor
            }

            val result = generatorRequest.filesToGenerate
                .map(descriptorsByName::getValue)
                .flatMap {
                    dtoGenerator.generate(it)
                }

            response(result).writeTo(output)
        }
    }

    private fun response(generatorRequest: List<FileSpec>): PluginProtos.CodeGeneratorResponse {
        val builder = PluginProtos.CodeGeneratorResponse.newBuilder()

        try {
            builder.setSupportedFeatures(PluginProtos.CodeGeneratorResponse.Feature.FEATURE_PROTO3_OPTIONAL_VALUE.toLong())
                .addAllFile(
                    generatorRequest.map { it.toFileResponse() }
                )
        } catch (failure: Exception) {
            builder.error = Throwables.getStackTraceAsString(failure)
        }

        return builder.build()
    }

    private fun FileSpec.toFileResponse() =
        PluginProtos.CodeGeneratorResponse.File.newBuilder().also {
            it.name = this.path.toString()
            it.content = this.toString()
        }.build()

    private fun prepareDtoGeneratorConfiguration(parameters: String): DtoGeneratorConfiguration {
        val configuration = parameters.split(",")
            .associate {
                val option = it.split("=")
                option[0] to option[1]
            }

        val dtoGeneratorConfiguration = DtoGeneratorConfiguration()

        val dtoSuffix = configuration[DTO_SUFFIX_PARAMETER_NAME]

        if (dtoSuffix != null) {
            dtoGeneratorConfiguration.dtoSuffix = dtoSuffix.capitalizeString()
        }

        val dtoPackageSuffix = configuration[DTO_PACKAGE_SUFFIX_PARAMETER_NAME]

        if (dtoPackageSuffix != null) {
            dtoGeneratorConfiguration.dtoPackageSuffix = dtoPackageSuffix.lowercase().replaceFirst(".", "")
        }

        return dtoGeneratorConfiguration
    }
}

data class ProtoFileName(private val path: String) : Comparable<ProtoFileName> {
    val name: String
        get() = path.substringAfterLast('/').removeSuffix(".proto")

    override operator fun compareTo(other: ProtoFileName): Int = path.compareTo(other.path)
}

/** Returns the filename of the specified file descriptor in proto form. */
val DescriptorProtos.FileDescriptorProto.fileName: ProtoFileName
    get() = ProtoFileName(name)

/** Returns the filename of the specified file descriptor. */
val Descriptors.FileDescriptor.fileName: ProtoFileName
    get() = toProto().fileName

val DescriptorProtos.FileDescriptorProto.dependencyNames: List<ProtoFileName>
    get() = dependencyList.map(::ProtoFileName)

val PluginProtos.CodeGeneratorRequest.filesToGenerate: List<ProtoFileName>
    get() = fileToGenerateList.map(::ProtoFileName)

val FileSpec.path: Path
    get() {
        return if (packageName.isEmpty()) {
            Paths.get("$name.kt")
        } else {
            path(*packageName.split('.').toTypedArray(), "$name.kt")
        }
    }

private fun path(vararg component: String): Path =
    Paths.get(component[0], *component.sliceArray(1 until component.size))