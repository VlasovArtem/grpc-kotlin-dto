package org.avlasov.kotlin.dto.generator

const val DTO_SUFFIX_PARAMETER_NAME = "dto_suffix"
const val DTO_PACKAGE_SUFFIX_PARAMETER_NAME = "dto_package_suffix"

data class DtoGeneratorConfiguration(
    var dtoSuffix: String = "Dto",
    var dtoPackageSuffix: String = "dto"
)
