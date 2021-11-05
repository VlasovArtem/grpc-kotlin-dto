package org.avlasov.kotlin.dto.generator.converter

import com.google.protobuf.Descriptors
import com.google.protobuf.Descriptors.Descriptor
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import org.avlasov.kotlin.dto.generator.DtoGeneratorConfiguration
import org.avlasov.kotlin.dto.generator.converter.utils.FunctionSpecGenerator
import org.avlasov.kotlin.dto.generator.converter.utils.FunctionType
import org.avlasov.kotlin.dto.generator.converter.utils.fileSpecBuilder
import org.avlasov.kotlin.dto.generator.converter.utils.messageClass
import org.avlasov.kotlin.dto.generator.converter.utils.toDtoClassName

class DescriptorConverter(
    private val config: DtoGeneratorConfiguration
): GenericDescriptorConverter<Descriptor> {

    private val fieldDescriptorConverter = FieldDescriptorConverter(config)

    override fun convert(descriptor: Descriptor): FileSpec {
        val messageClass = descriptor.messageClass()
        val dtoMessageClass = messageClass.toDtoClassName(config)

        val fileSpecBuilder = dtoMessageClass.fileSpecBuilder()

        val classBuilder = TypeSpec.classBuilder(dtoMessageClass)

        val constructorBuilder = FunSpec.constructorBuilder()

        descriptor.fields
            .forEach {
                val (parameterSpec, propertySpec) = fieldDescriptorConverter.convert(it)

                constructorBuilder.addParameter(parameterSpec)
                classBuilder.addProperty(propertySpec)
            }

        return fileSpecBuilder
            .addType(
                classBuilder
                    .primaryConstructor(constructorBuilder.build())
                    .addModifiers(KModifier.DATA)
                    .build()
            ).apply {
                addFunction(FunctionSpecGenerator.generate(FunctionType.DTO, descriptor, messageClass, dtoMessageClass))
                    .addImport(messageClass.packageName, messageClass.simpleName)

                addFunction(FunctionSpecGenerator.generate(FunctionType.GRPC, descriptor, dtoMessageClass, messageClass))
                    .addImport(messageClass.packageName, messageClass.simpleName)
            }
            .build()
    }

    override fun convert(fileDescriptors: Descriptors.FileDescriptor): List<FileSpec> = convert(fileDescriptors.messageTypes)
}