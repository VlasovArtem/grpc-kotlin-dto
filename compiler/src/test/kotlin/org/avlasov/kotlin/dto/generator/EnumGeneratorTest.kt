package org.avlasov.kotlin.dto.generator

import io.kotest.matchers.shouldBe
import org.avlasov.test.enums.TestEnum
import org.avlasov.test.enums.dto.TestEnumDto
import org.avlasov.test.enums.dto.toDto
import org.avlasov.test.enums.dto.toGrpc
import org.junit.jupiter.api.Test

class EnumGeneratorTest {

    @Test
    fun `generate enum object`() {
        TestEnum.FIRST.toDto() shouldBe TestEnumDto.FIRST
        TestEnum.SECOND.toDto() shouldBe TestEnumDto.SECOND
        TestEnum.THIRD.toDto() shouldBe TestEnumDto.THIRD

        TestEnumDto.FIRST.toGrpc() shouldBe TestEnum.FIRST
        TestEnumDto.SECOND.toGrpc() shouldBe TestEnum.SECOND
        TestEnumDto.THIRD.toGrpc() shouldBe TestEnum.THIRD
    }

}