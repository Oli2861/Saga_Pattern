package com.oli.order

import org.jetbrains.exposed.dao.id.IntIdTable

object OrderItems : IntIdTable() {
    val orderId = reference("orderId", Orders)
    val articleNumber = integer("articleNumber")
    val amount = integer("amount")
}