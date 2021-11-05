package org.avlasov.kotlin.dto.generator.converter.utils

import com.google.protobuf.Descriptors
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import org.avlasov.kotlin.dto.generator.DtoGeneratorConfiguration
import org.avlasov.kotlin.dto.generator.utils.PackageScope
import org.avlasov.kotlin.dto.generator.utils.builder
import org.avlasov.kotlin.dto.generator.utils.enumClassSimpleName
import org.avlasov.kotlin.dto.generator.utils.messageClassSimpleName
import org.avlasov.kotlin.dto.generator.utils.nestedClass
import org.avlasov.kotlin.dto.generator.utils.outerClassSimpleName

val special: Map<ClassName, ClassName> =
    mapOf(
        ClassName("com.google.protobuf", "Any")
            to ClassName("com.google.protobuf", "Any")
    )

val objectTypes: List<Descriptors.FieldDescriptor.JavaType> =
    listOf(Descriptors.FieldDescriptor.JavaType.MESSAGE, Descriptors.FieldDescriptor.JavaType.ENUM)

fun ClassName.toDtoClassName(config: DtoGeneratorConfiguration) =
    ClassName(
        packageName = "$packageName.${config.dtoPackageSuffix}",
        "$simpleName${config.dtoSuffix}"
    )

fun ClassName.fileSpecBuilder() =
    FileSpec.builder(this).indent("    ")

fun FileSpec.toClassName() = ClassName(packageName, name)

fun Descriptors.EnumDescriptor.enumClass(): ClassName {
    val contType: Descriptors.Descriptor? = containingType
    return when {
        contType != null -> contType.messageClass().nestedClass(enumClassSimpleName)
        file.options.javaMultipleFiles -> javaPackage(file).nestedClass(enumClassSimpleName)
        else -> file.outerClass().nestedClass(enumClassSimpleName)
    }
}

fun Descriptors.Descriptor.messageClass(): ClassName {
    val contType: Descriptors.Descriptor? = containingType
    return when {
        contType != null -> contType.messageClass().nestedClass(messageClassSimpleName)
        file.options.javaMultipleFiles -> javaPackage(file).nestedClass(messageClassSimpleName)
        else -> file.outerClass().nestedClass(messageClassSimpleName)
    }
}

fun javaPackage(fileDescriptor: Descriptors.FileDescriptor) =
    PackageScope(javaPackageString(fileDescriptor))

private fun javaPackageString(fileDescriptor: Descriptors.FileDescriptor): String =
    if (fileDescriptor.options.hasJavaPackage()) {
        fileDescriptor.options.javaPackage
    } else {
        fileDescriptor.`package`
    }

// Helpers on FileDescriptor.

/** Returns the fully qualified name of the outer class generated for this proto file. */
fun Descriptors.FileDescriptor.outerClass(): ClassName = javaPackage(this).nestedClass(outerClassSimpleName)