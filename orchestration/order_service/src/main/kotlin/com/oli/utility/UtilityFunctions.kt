package com.oli.utility

import org.apache.commons.text.StringEscapeUtils
import org.slf4j.Logger

fun stringIdToInt(id: String, logger: Logger): Int? = try {
    id.toInt()
} catch (e: NumberFormatException) {
    val sanitizedId = StringEscapeUtils.escapeJava(id)
    logger.error("Failed to parse id to number: $sanitizedId")
    null
}
