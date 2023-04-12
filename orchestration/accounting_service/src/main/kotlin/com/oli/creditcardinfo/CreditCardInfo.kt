package com.oli.creditcardinfo

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
data class CreditCardInfo(
    val userId: Int,
    val info: String
)

object CreditCardInfos: IntIdTable() {
    val userId = integer("userId")
    val info = varchar("info", 256)
}
