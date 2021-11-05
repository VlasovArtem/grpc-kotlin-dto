package org.avlasov.kotlin.dto.generator

import com.google.protobuf.Descriptors
import com.squareup.kotlinpoet.FileSpec
import org.avlasov.kotlin.dto.generator.converter.DescriptorConverter
import org.avlasov.kotlin.dto.generator.converter.EnumDescriptorConverter
import org.avlasov.kotlin.dto.generator.converter.GenericDescriptorConverter

class DtoGenerator(
    config: DtoGeneratorConfiguration
) {

    private val converters: List<GenericDescriptorConverter<*>> =
        listOf(
            DescriptorConverter(config),
            EnumDescriptorConverter(config)
        )

    fun generate(fileDescriptor: Descriptors.FileDescriptor): List<FileSpec> =
        converters.flatMap { it.convert(fileDescriptor) }
}
