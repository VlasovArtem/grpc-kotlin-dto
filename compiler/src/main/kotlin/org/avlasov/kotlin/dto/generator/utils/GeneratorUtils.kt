package org.avlasov.kotlin.dto.generator.utils

import java.util.Locale

fun String.capitalizeString(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }