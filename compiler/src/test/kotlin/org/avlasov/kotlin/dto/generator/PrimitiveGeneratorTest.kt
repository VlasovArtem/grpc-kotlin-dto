package org.avlasov.kotlin.dto.generator

import io.kotest.matchers.shouldBe
import org.avlasov.test.primitives.TestPrimitive
import org.avlasov.test.primitives.dto.TestPrimitiveDto
import org.avlasov.test.primitives.dto.toDto
import org.avlasov.test.primitives.dto.toGrpc
import org.junit.jupiter.api.Test

class PrimitiveGeneratorTest {

    @Test
    fun `generate primitives`() {
        TestPrimitive.getDefaultInstance().toDto() shouldBe TestPrimitiveDto()

        TestPrimitiveDto().toGrpc() shouldBe TestPrimitive.getDefaultInstance()

        val grpc = TestPrimitive.newBuilder()
            .setBooleanValue(true)
            .setDoubleValue(2.0)
            .setFixed32Value(32 * 2)
            .setFixed64Value(64 * 2L)
            .setFloatValue(32F)
            .setInt32Value(32)
            .setInt64Value(64L)
            .setSFixed32Value(32 * 3)
            .setSFixed64Value(64 * 3L)
            .setStringValue("new")
            .setSInt32Value(32 * 4)
            .setSInt64Value(64 * 4L)
            .setUInt32Value(32 * 5)
            .setUInt64Value(64 * 5L)
            .build()

        val dto = TestPrimitiveDto(
            stringValue = "new",
            doubleValue = 2.0,
            booleanValue = true,
            fixed32Value = 32 * 2,
            fixed64Value = 64 * 2L,
            floatValue = 32F,
            int32Value = 32,
            int64Value = 64,
            sFixed32Value = 32 * 3,
            sFixed64Value = 64 * 3L,
            sInt32Value = 32 * 4,
            sInt64Value = 64 * 4L,
            uInt32Value = 32 * 5,
            uInt64Value = 64 * 5L,
        )

        grpc.toDto() shouldBe dto
        dto.toGrpc() shouldBe grpc
    }

}