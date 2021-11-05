package org.avlasov.kotlin.dto.generator.converter.utils

import com.google.protobuf.Descriptors
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.GenericDescriptor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import mu.KLogging
import org.avlasov.kotlin.dto.generator.utils.capitalizeString

enum class FunctionType(val functionName: String) {
    DTO("toDto"),
    GRPC("toGrpc")
}

object FunctionSpecGenerator: KLogging() {

    fun generate(
        functionType: FunctionType,
        descriptor: GenericDescriptor,
        receiver: ClassName,
        returns: ClassName
    ): FunSpec =
        when(functionType) {
            FunctionType.DTO -> generateToDto(descriptor, receiver, returns)
            FunctionType.GRPC -> generateToGrpc(descriptor, receiver, returns)
        }

    private fun generateToDto(
        descriptor: GenericDescriptor,
        receiver: ClassName,
        returns: ClassName
    ): FunSpec =
        when (descriptor) {
            is Descriptor -> generateToDtoForClass(descriptor, receiver, returns)
            is Descriptors.EnumDescriptor -> generateToDtoForEnum(receiver, returns)
            else -> throw IllegalArgumentException("Descriptor type not supported ${descriptor.name}")
        }

    private fun generateToDtoForClass(
        descriptor: Descriptor,
        receiver: ClassName,
        returns: ClassName
    ): FunSpec =
        FunSpec.builder(FunctionType.DTO)
            .receiver(receiver)
            .returns(returns)
            .addCode(
                CodeBlock.builder()
                    .add("return \n\t%L(", returns.simpleName)
                    .apply {
                        add("\n")
                        descriptor.fields.forEachIndexed { index, description ->
                            description.generateDtoFieldLine(this, descriptor.fields.size - 1 == index)
                        }
                    }
                    .add("\t)\n")
                    .build()
            )
            .build()

    private fun generateToDtoForEnum(
        enumClassName: ClassName,
        dtoEnumClassName: ClassName
    ): FunSpec =
        FunSpec.builder(FunctionType.DTO)
            .receiver(enumClassName)
            .returns(dtoEnumClassName)
            .addStatement("return %L.valueOf(name)", dtoEnumClassName.simpleName)
            .build()

    private fun FieldDescriptor.generateDtoFieldLine(builder: CodeBlock.Builder, isLast: Boolean) {
        val converter = getConverter(FunctionType.DTO)
        val comma = if (isLast) "" else ","

        if (isRepeated && containingOneof == null) {
            if (isConverterRequired()) {
                logger.error { messageType.messageClass() }
                builder
                    .addStatement("\t\t%L = %L.map { %L }%L", name, "${name}List", "it$converter", comma)
            } else {
                builder
                    .addStatement("\t\t%L = %L%L", name, "${name}List", comma)
            }
        } else {
            if (containingOneof != null) {
                builder
                    .addStatement("\t\t%L = if (%L) %L else %L", name, "has${name.capitalizeString()}()", "$name$converter", "null$comma")
            } else {
                builder
                    .addStatement("\t\t%L = %L", name, "$name$converter$comma")
            }
        }
    }

    private fun generateToGrpc(
        descriptor: GenericDescriptor,
        receiver: ClassName,
        returns: ClassName
    ): FunSpec =
        when (descriptor) {
            is Descriptor -> generateToGrpcForClass(descriptor, receiver, returns)
            is Descriptors.EnumDescriptor -> generateToGrpcForEnum(receiver, returns)
            else -> throw IllegalArgumentException("Descriptor type not supported ${descriptor.name}")
        }

    private fun generateToGrpcForEnum(
        receiver: ClassName,
        returns: ClassName
    ): FunSpec =
        FunSpec.builder(FunctionType.GRPC)
            .receiver(receiver)
            .returns(returns)
            .addStatement("return %L.valueOf(name)", returns.simpleName)
            .build()

    private fun generateToGrpcForClass(
        descriptor: Descriptor,
        dtoClassName: ClassName,
        messageClassName: ClassName
    ): FunSpec =
        FunSpec.builder(FunctionType.GRPC)
            .receiver(dtoClassName)
            .returns(messageClassName)
            .addCode(
                CodeBlock.builder().apply {
                    beginControlFlow("return \n\t%L.newBuilder().also", messageClassName.simpleName)
                        .apply {
                            val (fields, fieldsAsOneOf) =
                                descriptor.fields.partition {
                                    it.containingOneof == null
                                        && (it.isRepeated || it.javaType !in objectTypes)
                                        && !it.hasOptionalKeyword()
                                }

                            fields.forEach {
                                it.generateGrpcFieldLine(this)
                            }

                            fieldsAsOneOf.forEach {
                                indent()
                                    .beginControlFlow("if (%L != null)", it.name)
                                    .addStatement("it.%L = %L${it.getConverter(FunctionType.GRPC)}", it.name, it.name)
                                    .endControlFlow()
                                    .unindent()
                            }
                        }.indent()
                        .endControlFlow()
                        .add(".build()")
                }.build()
            )
            .build()


    private fun FieldDescriptor.generateGrpcFieldLine(builder: CodeBlock.Builder) {
        val converter = getConverter(FunctionType.GRPC)

        if (isRepeated) {
            if (isConverterRequired()) {
                builder
                    .addStatement("\tit.addAll%L(%L.map { %L })", name.capitalizeString(), name, "o -> o$converter")
            } else {
                builder
                    .addStatement("\tit.addAll%L(%L)", name.capitalizeString(), name)
            }
        } else {
            builder
                .addStatement("\tit.%L = %L%L", name, name, converter)
        }
    }

    private fun FieldDescriptor.getConverter(functionType: FunctionType): String =
        if (isConverterRequired()) {
            ".${functionType.functionName}()"
        } else ""

    private fun FunSpec.Companion.builder(functionType: FunctionType) =
        builder(functionType.functionName)

    private fun FieldDescriptor.isConverterRequired() =
        if (javaType == FieldDescriptor.JavaType.MESSAGE) {
            special[messageType.messageClass()] == null
        } else {
            javaType in objectTypes
        }
}
