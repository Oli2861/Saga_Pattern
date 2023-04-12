package com.oli.persistence

import com.oli.order.*
import com.oli.proxies.Address
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.sql.Timestamp

class OrderDAOImpl : OrderDAO {

    private fun resultRowToOrder(resultRow: ResultRow, items: List<OrderItem>) = Order(
        id = resultRow[Orders.id].value,
        customerId = resultRow[Orders.customerId],
        address = Address(
            postCode = resultRow[Orders.postCode],
            city = resultRow[Orders.city],
            houseNumber = resultRow[Orders.houseNumber],
        ),
        paymentInfo = resultRow[Orders.paymentInfo],
        timestamp = Timestamp.from(resultRow[Orders.date]),
        orderState = resultRow[Orders.state],
        items = items
    )

    private fun resultRowToOrderItem(resultRow: ResultRow) =
        OrderItem(articleNumber = resultRow[OrderItems.articleNumber], amount = resultRow[OrderItems.amount])

    override suspend fun createOrder(order: Order): Order? = DatabaseFactory.dbQuery {
        val createOrderId = Orders.insertAndGetId {
            it[customerId] = order.customerId
            it[postCode] = order.address.postCode
            it[city] = order.address.city
            it[houseNumber] = order.address.houseNumber
            it[paymentInfo] = order.paymentInfo
            it[date] = order.timestamp.toInstant()
            it[state] = order.orderState
        }.value

        order.items.forEach { orderItem ->
            OrderItems.insert {
                it[orderId] = createOrderId
                it[articleNumber] = orderItem.articleNumber
                it[amount] = orderItem.amount
            }
        }

        return@dbQuery readOrderQuery(createOrderId)
    }

    private fun readOrderQuery(id: Int): Order? {
        val items = OrderItems.select { OrderItems.orderId eq id }.map(::resultRowToOrderItem)
        return Orders.select { Orders.id eq id }.map { resultRowToOrder(it, items) }.firstOrNull()
    }

    override suspend fun readOrder(id: Int) = DatabaseFactory.dbQuery { readOrderQuery(id) }

    override suspend fun deleteOrder(id: Int): Int = DatabaseFactory.dbQuery {
        OrderItems.deleteWhere { this.orderId eq id }
        Orders.deleteWhere { this.id eq id }
    }

    override suspend fun updateOrderState(id: Int, newState: Int): Int = DatabaseFactory.dbQuery {
        Orders.update({ Orders.id eq id }) {
            it[state] = newState
        }
    }

}