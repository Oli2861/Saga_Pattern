package com.oli.persistence

import com.oli.order.Order
import com.oli.order.OrderDAO
import com.oli.order.OrderItem
import com.oli.proxies.Address
import com.oli.saga.CreateOrderSagaState
import com.oli.saga.CreateOrderSagaStateDAO
import kotlinx.coroutines.runBlocking
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertEquals

class OrderSagaDAOTest {
    companion object {
        private lateinit var createOrderSagaStateDAO: CreateOrderSagaStateDAO
        private lateinit var orderDAO: OrderDAO

        @BeforeClass
        @JvmStatic
        fun initialize() {
            DatabaseFactory.init(true)
            orderDAO = OrderDAOImpl()
            createOrderSagaStateDAO = CreateOrderSagaStateDAOImpl()
        }
    }

    @Test
    fun testCreate() = runBlocking {
        val order = orderDAO.createOrder(Order(customerId = 1, address = Address(12345, "Mustertown", "5e"), paymentInfo = "test", items= listOf(OrderItem(1, 1))))!!
        val created = createOrderSagaStateDAO.create(CreateOrderSagaState(0, 1, false, order.id))

        assertEquals(1, created!!.currentStep)
        assertEquals(false, created.rollingBack)
    }

    @Test
    fun testRead() = runBlocking {
        val order = orderDAO.createOrder(Order(customerId = 1, address = Address(12345, "Mustertown", "5e"), paymentInfo = "test", items= listOf(OrderItem(1, 1))))!!
        val created = createOrderSagaStateDAO.create(
            CreateOrderSagaState(0, 1, false, order.id))
        val readValue = createOrderSagaStateDAO.read(created!!.sagaId)!!

        assertEquals(1, readValue.currentStep)
        assertEquals(false, readValue.rollingBack)
    }

    @Test
    fun testDelete() = runBlocking {
        val order = orderDAO.createOrder(Order(customerId = 1, address = Address(12345, "Mustertown", "5e"), paymentInfo = "test", items= listOf(OrderItem(1, 1))))!!
        val created = createOrderSagaStateDAO.create(CreateOrderSagaState(0, 1, false, order.id))
        val retVal = createOrderSagaStateDAO.delete(created!!.sagaId)
        val readValue = createOrderSagaStateDAO.read(created.sagaId)

        assertEquals(1, retVal)
        assertEquals(null, readValue)
    }

}