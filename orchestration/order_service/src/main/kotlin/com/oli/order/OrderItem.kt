package com.oli.order

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
data class OrderItem(val articleNumber: Int, val amount: Int)

object OrderItems : IntIdTable() {
    val orderId = reference("orderId", Orders)
    val articleNumber = integer("articleNumber")
    val amount = integer("amount")
}
