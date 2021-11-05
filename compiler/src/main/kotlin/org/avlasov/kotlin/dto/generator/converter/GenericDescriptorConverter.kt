package org.avlasov.kotlin.dto.generator.converter

import com.google.protobuf.Descriptors
import com.squareup.kotlinpoet.FileSpec

interface GenericDescriptorConverter <T: Descriptors.GenericDescriptor> {
    
    fun convert(descriptor: T): FileSpec
    
    fun convert(descriptors: List<T>): List<FileSpec> =
        descriptors.map(::convert)

    fun convert(fileDescriptors: Descriptors.FileDescriptor): List<FileSpec>
}