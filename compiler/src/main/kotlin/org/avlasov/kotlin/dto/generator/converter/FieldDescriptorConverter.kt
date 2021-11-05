package org.avlasov.kotlin.dto.generator.converter

import com.google.protobuf.Descriptors
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import org.avlasov.kotlin.dto.generator.DtoGeneratorConfiguration
import org.avlasov.kotlin.dto.generator.converter.utils.enumClass
import org.avlasov.kotlin.dto.generator.converter.utils.messageClass
import org.avlasov.kotlin.dto.generator.converter.utils.special
import org.avlasov.kotlin.dto.generator.converter.utils.toDtoClassName
import org.avlasov.kotlin.dto.generator.utils.isJavaPrimitive
import kotlin.reflect.jvm.isAccessible

class FieldDescriptorConverter(
    private val config: DtoGeneratorConfiguration
) {
    private val nonNullableTypes: List<Descriptors.FieldDescriptor.JavaType> =
        listOf(Descriptors.FieldDescriptor.JavaType.STRING)

    fun convert(fieldDescriptor: Descriptors.FieldDescriptor): Pair<ParameterSpec, PropertySpec> {
        val typeName = fieldDescriptor
            .createKotlinType()
            .asTypeName()

        return with(fieldDescriptor) {
            buildParameterSpec(typeName) to buildPropertySpec(typeName)
        }
    }

    private fun Descriptors.FieldDescriptor.buildParameterSpec(typeName: TypeName) =
        ParameterSpec.builder(name, typeName)
            .defaultValue(getInitializer())
            .build()

    private fun Descriptors.FieldDescriptor.buildPropertySpec(typeName: TypeName) =
        PropertySpec.builder(name, typeName)
            .initializer(name)
            .build()

    private fun Descriptors.FieldDescriptor.getInitializer(): String =
        with(javaType) {
            this::class.members
                .first { it.name == "defaultDefault" }
                .let {
                    if (isRepeated) {
                        "listOf()"
                    } else {
                        when (this) {
                            Descriptors.FieldDescriptor.JavaType.STRING -> {
                                if (containingOneof != null) "null"
                                else "\"\""
                            }
                            Descriptors.FieldDescriptor.JavaType.FLOAT -> "0F"
                            else -> {
                                it.isAccessible = true

                                it.call(this).toString()
                            }
                        }
                    }
                }
        }

    private fun Descriptors.FieldDescriptor.createKotlinType() = when (javaType) {
        Descriptors.FieldDescriptor.JavaType.MESSAGE ->
            generateObjectKotlinType(
                messageType.messageClass().getDtoClassName()
            )
        Descriptors.FieldDescriptor.JavaType.ENUM ->
            generateObjectKotlinType(
                enumType.enumClass().getDtoClassName()
            )
        else -> generateSimpleKotlinType()
    }

    private fun Descriptors.FieldDescriptor.generateSimpleKotlinType(): KotlinField =
        when (javaType) {
            Descriptors.FieldDescriptor.JavaType.STRING -> STRING
            Descriptors.FieldDescriptor.JavaType.INT -> INT
            Descriptors.FieldDescriptor.JavaType.BOOLEAN -> BOOLEAN
            Descriptors.FieldDescriptor.JavaType.FLOAT -> FLOAT
            Descriptors.FieldDescriptor.JavaType.DOUBLE -> DOUBLE
            Descriptors.FieldDescriptor.JavaType.LONG -> LONG
            else -> throw IllegalArgumentException("$javaType is not supported")
        }.let { className ->
            KotlinField(
                className = className,
                nullable = isSimpleKotlinTypeNullable(),
                repeated = isRepeated
            )
        }

    private fun Descriptors.FieldDescriptor.generateObjectKotlinType(
        className: ClassName
    ) = KotlinField(
        className = className,
        nullable = true,
        repeated = isRepeated
    )

    private fun KotlinField.asTypeName(): TypeName {
        return if (repeated) {
            LIST.parameterizedBy(className)
        } else {
            className.copy(nullable)
        }
    }

    private data class KotlinField(
        val className: ClassName,
        val nullable: Boolean,
        val repeated: Boolean
    )

    private fun Descriptors.FieldDescriptor.isSimpleKotlinTypeNullable() =
        containingOneof != null ||
            javaType !in nonNullableTypes && !isJavaPrimitive && hasOptionalKeyword()

    private fun ClassName.getDtoClassName() =
        special[this] ?: toDtoClassName(config)

}