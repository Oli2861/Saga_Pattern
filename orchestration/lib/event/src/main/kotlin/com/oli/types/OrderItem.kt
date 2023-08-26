package com.oli.types

import kotlinx.serialization.Serializable

@Serializable
data class OrderItem(val articleNumber: Int, val amount: Int) {
    fun equalIgnoreTicketId(other: Any?): Boolean {
        if (other !is OrderItem) return false
        if (other.articleNumber != articleNumber) return false
        if (other.amount != amount) return false
        return true
    }
}
