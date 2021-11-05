package org.avlasov.kotlin.dto.generator.converter

import com.google.protobuf.Descriptors
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import org.avlasov.kotlin.dto.generator.DtoGeneratorConfiguration
import org.avlasov.kotlin.dto.generator.converter.utils.FunctionSpecGenerator
import org.avlasov.kotlin.dto.generator.converter.utils.FunctionType
import org.avlasov.kotlin.dto.generator.converter.utils.enumClass
import org.avlasov.kotlin.dto.generator.converter.utils.fileSpecBuilder
import org.avlasov.kotlin.dto.generator.converter.utils.toDtoClassName

class EnumDescriptorConverter(
    private val config: DtoGeneratorConfiguration
) : GenericDescriptorConverter<Descriptors.EnumDescriptor> {

    override fun convert(descriptor: Descriptors.EnumDescriptor): FileSpec {
        val enumClass = descriptor.enumClass()
        val dtoEnumClass = enumClass.toDtoClassName(config)

        val fileSpecBuilder = dtoEnumClass.fileSpecBuilder()

        val enumTypeBuilder = TypeSpec.enumBuilder(dtoEnumClass)

        descriptor.values
            .forEach {
                enumTypeBuilder.addEnumConstant(it.name)
            }

        return fileSpecBuilder
            .addType(enumTypeBuilder.build())
            .apply {
                addFunction(FunctionSpecGenerator.generate(FunctionType.DTO, descriptor, enumClass, dtoEnumClass))
                    .addImport(enumClass.packageName, enumClass.simpleName)

                addFunction(FunctionSpecGenerator.generate(FunctionType.GRPC, descriptor, dtoEnumClass, enumClass))
                    .addImport(enumClass.packageName, enumClass.simpleName)
            }
            .build()
    }

    override fun convert(fileDescriptors: Descriptors.FileDescriptor): List<FileSpec> =
        convert(fileDescriptors.enumTypes)
}