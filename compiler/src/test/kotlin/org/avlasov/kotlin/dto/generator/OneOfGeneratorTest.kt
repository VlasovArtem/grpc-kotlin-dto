package org.avlasov.kotlin.dto.generator

import io.kotest.matchers.shouldBe
import org.avlasov.test.oneof.OneOfEnum
import org.avlasov.test.oneof.OneOfMessageFirst
import org.avlasov.test.oneof.OneOfMessageSecond
import org.avlasov.test.oneof.TestOneOf
import org.avlasov.test.oneof.dto.OneOfMessageSecondDto
import org.avlasov.test.oneof.dto.TestOneOfDto
import org.avlasov.test.oneof.dto.toDto
import org.avlasov.test.oneof.dto.toGrpc
import org.junit.jupiter.api.Test

class OneOfGeneratorTest {

    @Test
    fun `generate one of with default`() {
        TestOneOf.getDefaultInstance().toDto() shouldBe TestOneOfDto()
        TestOneOfDto().toGrpc() shouldBe TestOneOf.getDefaultInstance()
    }

    @Test
    fun `generate one of with single field`() {
        TestOneOfDto("hello").toGrpc() shouldBe TestOneOf
            .newBuilder()
            .setStringOneOf("hello")
            .build()

        TestOneOf
            .newBuilder()
            .setStringOneOf("hello")
            .build().toDto() shouldBe TestOneOfDto("hello")
    }

    @Test
    fun `generate one of with all fields`() {
        val dto = TestOneOfDto(
            secondMessage = OneOfMessageSecondDto(2.0)
        )
        val grpc = TestOneOf
            .newBuilder()
            .setStringOneOf("hello")
            .setFirstMessage(
                OneOfMessageFirst.newBuilder()
                    .setEnumValue(OneOfEnum.ONE_OF_ELEMENT)
                    .build()
            )
            .setSecondMessage(
                OneOfMessageSecond.newBuilder()
                    .setDoubleValue(2.0)
                    .build()
            )
            .build()

        dto.toGrpc() shouldBe grpc

        grpc.toDto() shouldBe dto
    }

}