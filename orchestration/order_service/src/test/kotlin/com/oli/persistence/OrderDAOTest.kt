package com.oli.persistence

import com.oli.order.Order
import com.oli.order.OrderItem
import com.oli.proxies.Address
import com.oli.saga.EntityStates
import kotlinx.coroutines.runBlocking
import org.junit.BeforeClass
import org.junit.Test
import java.sql.Timestamp
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrderDAOTest {
    companion object {
        private lateinit var dao: OrderDAOImpl

        @BeforeClass
        @JvmStatic
        fun initialize() {
            DatabaseFactory.init(true)
            dao = OrderDAOImpl()
        }
    }

    @Test
    fun testInsert() = runBlocking {
        val order = Order(
            id = 0,
            customerId = 1,
            address = Address(1234, "askdj", "5c"),
            paymentInfo = "jahsd",
            orderState = 1,
            items = listOf(OrderItem(0, 1), OrderItem(2, 5), OrderItem(4, 10))
        )
        val actual = dao.createOrder(order)
        assertTrue(actual!!.id > 0)
        assertTrue(order.equalsIgnoreId(actual))
        order.items.forEach { assertTrue(actual.items.contains(it)) }
    }

    @Test
    fun testReadOrder() = runBlocking {
        val order = insertSampleOrder()
        val actual = dao.readOrder(order.id)!!
        assertTrue(actual.id > 0)
        assertTrue(order.equalsIgnoreId(actual))
    }

    @Test
    fun testDeleteOrder() = runBlocking {
        val order = insertSampleOrder()
        val amountOfDeletedEntries = dao.deleteOrder(order.id)
        val readAttempt = dao.readOrder(order.id)

        assertEquals(1, amountOfDeletedEntries)
        assertEquals(null, readAttempt)

    }

    @Test
    fun testUpdateOrderState() = runBlocking {
        val order = insertSampleOrder()
        val affectedRows = dao.updateOrderState(order.id, EntityStates.CANCELED)
        val updatedEntry = dao.readOrder(order.id)!!

        assertEquals(1, affectedRows)
        assertEquals(order.id, updatedEntry.id)
        assertEquals(EntityStates.CANCELED, updatedEntry.orderState)

    }

    private suspend fun insertSampleOrder(): Order {
        val order = Order(
            id = 0,
            customerId = 1,
            address = Address(1234, "askdj", "5c"),
            paymentInfo = "jahsd",
            orderState = 1,
            items = listOf(OrderItem(0, 1), OrderItem(2, 5), OrderItem(4, 10))
        )
        return dao.createOrder(order)!!
    }

}