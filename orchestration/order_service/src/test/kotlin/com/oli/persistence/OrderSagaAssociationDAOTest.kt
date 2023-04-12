package com.oli.persistence

import com.oli.order.Order
import com.oli.order.OrderDAO
import com.oli.order.OrderItem
import com.oli.order.OrderSagaAssociationDAO
import com.oli.proxies.Address
import com.oli.proxies.Customer
import com.oli.saga.CreateOrderSagaState
import com.oli.saga.CreateOrderSagaStateDAO
import kotlinx.coroutines.runBlocking
import org.junit.BeforeClass
import org.junit.Test
import java.sql.Timestamp
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OrderSagaAssociationDAOTest {

    companion object {
        private lateinit var orderDAO: OrderDAO
        private lateinit var sagaStateDAO: CreateOrderSagaStateDAO
        private lateinit var orderSagaAssociationDAO: OrderSagaAssociationDAO

        @BeforeClass
        @JvmStatic
        fun initialize() {
            DatabaseFactory.init(true)
            orderDAO = OrderDAOImpl()
            sagaStateDAO = CreateOrderSagaStateDAOImpl()
            orderSagaAssociationDAO = OrderSagaAssociationDAOImpl()
        }
    }

    @Test
    fun testCreate() = runBlocking {
        val order = orderDAO.createOrder(Order(customerId = 1, address = Address(12345, "Mustertown", "5e"), paymentInfo = "test", items= listOf(OrderItem(1, 1))))!!
        val createOrderSagaState = sagaStateDAO.create(CreateOrderSagaState(0, 0, false, order.id))
        val orderSagaAssociation = orderSagaAssociationDAO.create(order.id, createOrderSagaState!!.sagaId)

        assertEquals(order.id, orderSagaAssociation!!.orderId)
        assertEquals(createOrderSagaState.sagaId, orderSagaAssociation.sagaId)
    }

    @Test
    fun testReadUsingSagaId() = runBlocking {
        val order = orderDAO.createOrder(Order(customerId = 1, address = Address(12345, "Mustertown", "5e"), paymentInfo = "test", items= listOf(OrderItem(1, 1))))
        val createOrderSagaState = sagaStateDAO.create(CreateOrderSagaState(0, 0, false, order!!.id))
        orderSagaAssociationDAO.create(order.id, createOrderSagaState!!.sagaId)
        val association = orderSagaAssociationDAO.readUsingSagaId(createOrderSagaState.sagaId)!!

        assertEquals(order.id, association.orderId)
        assertEquals(createOrderSagaState.sagaId, association.sagaId)
    }

    @Test
    fun testDeleteAllForSaga() = runBlocking {
        val order = orderDAO.createOrder(Order(customerId = 1, address = Address(12345, "Mustertown", "5e"), paymentInfo = "test", items= listOf(OrderItem(1, 1))))
        val createOrderSagaState = sagaStateDAO.create(CreateOrderSagaState(0, 0, false, order!!.id))
        orderSagaAssociationDAO.create(order.id, createOrderSagaState!!.sagaId)

        val retVal = orderSagaAssociationDAO.deleteAllForSaga(createOrderSagaState.sagaId)
        val association = orderSagaAssociationDAO.readUsingSagaId(createOrderSagaState.sagaId)

        assertEquals(1, retVal)
        assertNull(association)
    }

}