package com.oli.ticket

import com.oli.types.OrderItem
import org.jetbrains.exposed.dao.id.IntIdTable

data class OrderItemWithTicketId(val ticketId: Int, val orderItem: OrderItem)

object OrderItems : IntIdTable() {
    val ticketId = reference("ticketId", Tickets.id)
    val itemId = integer("itemId")
    val amount = integer("amount")
}
