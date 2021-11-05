package org.avlasov.kotlin.dto.generator

import io.kotest.matchers.shouldBe
import org.avlasov.test.optional.OptionalObject
import org.avlasov.test.optional.TestOptional
import org.avlasov.test.optional.dto.OptionalObjectDto
import org.avlasov.test.optional.dto.TestOptionalDto
import org.avlasov.test.optional.dto.toDto
import org.avlasov.test.optional.dto.toGrpc
import org.junit.jupiter.api.Test

class OptionalGeneratorTest {

    @Test
    fun `generate optional with default instance`() {
        TestOptional.getDefaultInstance().toDto() shouldBe TestOptionalDto()
        TestOptionalDto().toGrpc() shouldBe TestOptional.getDefaultInstance()
    }

    @Test
    fun `generate optional with primitive`() {
        val testOptional = TestOptional.newBuilder().setStr("hello").build()
        val testOptionalDto = TestOptionalDto(str = "hello")

        testOptional.toDto() shouldBe testOptionalDto
        testOptionalDto.toGrpc() shouldBe testOptional
    }

    @Test
    fun `generate optional with object`() {
        val testOptional = TestOptional.newBuilder().setOptionalObject(
            OptionalObject.newBuilder().setTest(true).build()
        ).build()
        val testOptionalDto = TestOptionalDto(OptionalObjectDto(true))

        testOptional.toDto() shouldBe testOptionalDto
        testOptionalDto.toGrpc() shouldBe testOptional
    }

    @Test
    fun `generate optional with all fields`() {
        val testOptional = TestOptional.newBuilder()
            .setOptionalObject(
                OptionalObject.newBuilder().setTest(true).build()
            )
            .setStr("hello")
            .build()
        val testOptionalDto = TestOptionalDto(OptionalObjectDto(true), "hello")

        testOptional.toDto() shouldBe testOptionalDto
        testOptionalDto.toGrpc() shouldBe testOptional
    }
}