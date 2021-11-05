package org.avlasov.kotlin.dto.generator

import io.kotest.matchers.shouldBe
import org.avlasov.test.repeated.RepeatedObject
import org.avlasov.test.repeated.TestRepeated
import org.avlasov.test.repeated.dto.RepeatedObjectDto
import org.avlasov.test.repeated.dto.TestRepeatedDto
import org.avlasov.test.repeated.dto.toDto
import org.avlasov.test.repeated.dto.toGrpc
import org.junit.jupiter.api.Test

class RepeatedGeneratorTest {

    @Test
    fun `generate repeated with default instance`() {
        TestRepeated.getDefaultInstance().toDto() shouldBe TestRepeatedDto()
        TestRepeatedDto().toGrpc() shouldBe TestRepeated.getDefaultInstance()
    }

    @Test
    fun `generate repeated with single field set`() {
        val testRepeated = TestRepeated
            .newBuilder()
            .addObjects(
                RepeatedObject.newBuilder().setTest(true).build()
            ).build()

        val testRepeatedDto = TestRepeatedDto(
            objects = listOf(RepeatedObjectDto(true))
        )

        testRepeated.toDto() shouldBe testRepeatedDto
        testRepeatedDto.toGrpc() shouldBe testRepeated
    }

    @Test
    fun `generate repeated with all field set`() {
        val testRepeated = TestRepeated
            .newBuilder()
            .addInts(1)
            .addStrings("hello")
            .addObjects(
                RepeatedObject.newBuilder().setTest(true).build()
            ).build()

        val testRepeatedDto = TestRepeatedDto(
            objects = listOf(RepeatedObjectDto(true)),
            strings = listOf("hello"),
            ints = listOf(1)
        )

        testRepeated.toDto() shouldBe testRepeatedDto
        testRepeatedDto.toGrpc() shouldBe testRepeated
    }

}