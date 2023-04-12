package com.oli.order

import com.oli.persistence.*
import com.oli.proxies.Address
import com.oli.proxies.Customer
import com.oli.saga.CreateOrderSagaState
import com.oli.saga.CreateOrderSagaStateDAO
import com.oli.saga.EntityStates
import kotlinx.coroutines.runBlocking
import org.junit.BeforeClass
import org.junit.Test
import java.sql.Timestamp
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OrderRepositoryTest {
    companion object {
        private lateinit var orderRepository: OrderRepository
        private lateinit var sagaStateDAO: CreateOrderSagaStateDAO
        private lateinit var orderSagaAssociationDAO: OrderSagaAssociationDAO
        private lateinit var orderDAO: OrderDAO

        @BeforeClass
        @JvmStatic
        fun init() {
            DatabaseFactory.init(true)
            sagaStateDAO = CreateOrderSagaStateDAOImpl()
            orderSagaAssociationDAO = OrderSagaAssociationDAOImpl()
            orderDAO = OrderDAOImpl()
            orderRepository = OrderRepositoryImpl(orderDAO, orderSagaAssociationDAO)
        }
    }

    @Test
    fun testCreateOrder() = runBlocking {
        val order = Order(customerId = 1, address = Address(12345, "Mustertown", "5e"), paymentInfo = "test", items= listOf(OrderItem(1, 1)))
        val saga = sagaStateDAO.create(CreateOrderSagaState(0, 0, false, null))
        val (createdOrder: Order?, createdAssociation: OrderSagaAssociation?) = orderRepository.createOrder(saga!!.sagaId, order)

        assertNotNull(createdOrder)
        assertTrue(order.equalsIgnoreId(createdOrder))

        assertNotNull(createdAssociation)
        assertEquals(saga.sagaId, createdAssociation.sagaId)
        assertEquals(createdOrder.id, createdAssociation.orderId)
    }

    @Test
    fun testReadOrder() = runBlocking {
        val order = Order(customerId = 1, address = Address(12345, "Mustertown", "5e"), paymentInfo = "test", items= listOf(OrderItem(1, 1)))
        val saga = sagaStateDAO.create(CreateOrderSagaState(0, 0, false, null))
        val (createdOrder: Order?, createdAssociation: OrderSagaAssociation?) = orderRepository.createOrder(saga!!.sagaId, order)
        val read = orderRepository.readOrder(createdOrder!!.id)

        assertTrue(order.equalsIgnoreId(read))
        assertTrue(createdOrder.equalsIgnoreId(read))
    }

    @Test
    fun testDeleteOrder() = runBlocking {
        val order = Order(customerId = 1, address = Address(12345, "Mustertown", "5e"), paymentInfo = "test", items= listOf(OrderItem(1, 1)))
        val saga = sagaStateDAO.create(CreateOrderSagaState(0, 0, false, null))
        val (created: Order?, createdAssociation: OrderSagaAssociation?) = orderRepository.createOrder(saga!!.sagaId, order)

        val affectedRows = orderRepository.deleteOrder(created!!.id)
        assertEquals(1, affectedRows)

        val readAttempt = orderRepository.readOrder(created.id)
        assertNull(readAttempt)

        // Verify that associations are deleted as well.
        val association = orderSagaAssociationDAO.readUsingSagaId(saga.sagaId)
        assertNull(association)
    }

    @Test
    fun testUpdateOrderState() = runBlocking {
        val originalorder = Order(customerId = 1, address = Address(12345, "Mustertown", "5e"), paymentInfo = "test", items= listOf(OrderItem(1, 1)))
        val saga = sagaStateDAO.create(CreateOrderSagaState(0, 0, false, null))
        val (created: Order?, createdAssociation: OrderSagaAssociation?) = orderRepository.createOrder(saga!!.sagaId, originalorder)

        val updatedRows = orderRepository.updateOrderState(saga.sagaId, EntityStates.CANCELED)
        assertEquals(1, updatedRows)

        val order = Order(customerId = 1, timestamp = originalorder.timestamp, address = Address(12345, "Mustertown", "5e"), orderState = EntityStates.CANCELED, paymentInfo = "test", items= listOf(OrderItem(1, 1)))
        val read = orderRepository.readOrder(created!!.id)
        assertTrue(order.equalsIgnoreId(read))
    }

}