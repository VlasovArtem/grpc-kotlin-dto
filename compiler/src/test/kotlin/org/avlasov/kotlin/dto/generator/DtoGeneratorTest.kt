package org.avlasov.kotlin.dto.generator

import com.google.protobuf.Descriptors
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import io.kotest.matchers.shouldBe
import org.avlasov.kotlin.dto.generator.converter.utils.enumClass
import org.avlasov.kotlin.dto.generator.converter.utils.messageClass
import org.avlasov.test.oneof.OneofTest
import org.avlasov.test.repeated.RepeatedObject
import org.avlasov.test.repeated.RepeatedTest
import org.avlasov.test.repeated.TestRepeated
import org.avlasov.test.repeated.dto.RepeatedObjectDto
import org.avlasov.test.repeated.dto.TestRepeatedDto
import org.avlasov.test.repeated.dto.toDto
import org.avlasov.test.repeated.dto.toGrpc
import org.avlasov.test.special.SpecialTest
import org.junit.jupiter.api.Test

class DtoGeneratorTest {

    companion object {
        private val config = DtoGeneratorConfiguration()
        private val dtoGenerator = DtoGenerator(config)
    }

    @Test
    fun name() {
        val fileSpecs = dtoGenerator.generate(SpecialTest.getDescriptor())
        val fileSpecs1 = dtoGenerator.generate(RepeatedTest.getDescriptor())
        val fileSpecs2 = dtoGenerator.generate(OneofTest.getDescriptor())
    }

    private fun Descriptors.EnumDescriptor.toClassName() =
        enumClass()

    private fun Descriptors.Descriptor.toClassName() =
        messageClass()

    private fun FileSpec.toClassName() =
        ClassName(packageName, name)

    private fun toClassName(packageName: String, name: String) =
        ClassName(packageName, name)
}