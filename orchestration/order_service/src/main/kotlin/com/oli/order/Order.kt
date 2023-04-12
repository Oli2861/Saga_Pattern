package com.oli.order

import com.oli.plugins.TimestampAsLongSerializer
import com.oli.proxies.Address
import com.oli.saga.EntityStates
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.sql.Timestamp

@Serializable
data class Order(
    val id: Int = 0,
    val customerId: Int,
    val address: Address,
    val paymentInfo: String,
    @Serializable(with = TimestampAsLongSerializer::class)
    val timestamp: Timestamp = Timestamp(System.currentTimeMillis()),
    val orderState: Int = EntityStates.PENDING,
    val items: List<OrderItem>
) {

    fun equalsIgnoreId(other: Any?): Boolean {
        if (other !is Order) return false
        if (other.customerId != customerId) return false
        if (other.address != address) return false
        if (other.paymentInfo != paymentInfo) return false
        if (other.timestamp != timestamp) return false
        if (other.orderState != orderState) return false
        if (other.items != items) return false
        return true
    }
}

object Orders : IntIdTable() {
    val customerId = integer("userId")

    val postCode = integer("postCode")
    val city = varchar("city", 64)
    val houseNumber = varchar("houseNumber", 16)

    val paymentInfo = varchar("paymentInfo", 256)

    val date = timestamp("timestamp")
    val state = integer("orderState")
}
