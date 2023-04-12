package com.oli.saga

import com.oli.order.*
import com.oli.persistence.*
import com.oli.proxies.*
import kotlinx.coroutines.runBlocking
import org.junit.BeforeClass
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.mockito.Mockito
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CreateOrderSagaTest {
    companion object {
        private lateinit var sagaDefinition: SagaDefinition
        private val logger: Logger = LoggerFactory.getLogger(CreateOrderSagaTest::class.java.name)
        private val customerServiceProxyMock: CustomerServiceProxy = Mockito.mock(CustomerServiceProxy::class.java)
        private val kitchenServiceProxyMock: KitchenServiceProxy = Mockito.mock(KitchenServiceProxy::class.java)
        private val accountingServiceProxyMock: AccountingServiceProxy = Mockito.mock(AccountingServiceProxy::class.java)
        private lateinit var createOrderSagaStateDAO: CreateOrderSagaStateDAO
        private lateinit var orderDAO: OrderDAO
        private lateinit var orderRepository: OrderRepository
        private lateinit var orderSagaAssociationDAO: OrderSagaAssociationDAO
        private lateinit var orderService: OrderService

        @BeforeClass
        @JvmStatic
        fun init(): Unit = runBlocking {
            DatabaseFactory.init(true)
            createOrderSagaStateDAO = CreateOrderSagaStateDAOImpl()
            orderDAO = OrderDAOImpl()
            orderSagaAssociationDAO = OrderSagaAssociationDAOImpl()
            orderRepository = OrderRepositoryImpl(orderDAO, orderSagaAssociationDAO)
            val createOrderSagaManager = CreateOrderSagaManager(createOrderSagaStateDAO, logger)
            orderService = OrderService(orderRepository, createOrderSagaManager, logger)

            // Koin
            startKoin {
                modules(
                    module {
                        // Actual order service instance and mocks for all services that are actually remote.
                        single<OrderServiceProxy> { orderService }
                        single<CustomerServiceProxy> { customerServiceProxyMock }
                        single<KitchenServiceProxy> { kitchenServiceProxyMock }
                        single<AccountingServiceProxy> { accountingServiceProxyMock }
                        single { LoggerFactory.getLogger("") }
                    }
                )
            }

        }
    }

    @Test
    fun testStep() = runBlocking {
        val order = Order(customerId = 1, address = Address(12345, "Mustertown", "5e"), paymentInfo = "test", items= listOf(OrderItem(1, 1)))
        val sagaState = createOrderSagaStateDAO.create(CreateOrderSagaState(0, 0, false, null))!!
        // Mock for remote services, actual service for order service
        sagaDefinition = CreateOrderSagaDefinition(sagaState, order)

        Mockito.`when`(customerServiceProxyMock.sendVerifyCustomerDetailsCommand(sagaState.sagaId, order.customerId, order.address)).thenReturn(true)
        Mockito.`when`(kitchenServiceProxyMock.createTicket(sagaState.sagaId, order.customerId, order.items)).thenReturn(true)
        Mockito.`when`(accountingServiceProxyMock.authorize(sagaState.sagaId, order.customerId, order.paymentInfo)).thenReturn(true)
        Mockito.`when`(kitchenServiceProxyMock.approveTicket(sagaState.sagaId)).thenReturn(true)

        for(i in 0..4){
            val retVal = sagaDefinition.step()
            assertEquals(SagaStepResult.UNFINISHED, retVal)
        }
        assertEquals(SagaStepResult.FINISHED, sagaDefinition.step())
    }

    @Test
    fun testRollback() = runBlocking {
        val order: Order = Order(customerId = 1, address = Address(12345, "Mustertown", "5e"), paymentInfo = "test", items= listOf(OrderItem(1, 1)))
        val sagaState = createOrderSagaStateDAO.create(CreateOrderSagaState(0, 0, false, null))!!
        // Mock for remote services, actual service for order service
        sagaDefinition = CreateOrderSagaDefinition(sagaState, order)

        // Forward step calls
        Mockito.`when`(customerServiceProxyMock.sendVerifyCustomerDetailsCommand(sagaState.sagaId, order.customerId, order.address)).thenReturn(true)
        Mockito.`when`(kitchenServiceProxyMock.createTicket(sagaState.sagaId, order.customerId, order.items)).thenReturn(true)
        Mockito.`when`(accountingServiceProxyMock.authorize(sagaState.sagaId, order.customerId, order.paymentInfo)).thenReturn(false)

        for(i in 0..2){
            val retVal = sagaDefinition.step()
            assertEquals(SagaStepResult.UNFINISHED, retVal)
            assertFalse(sagaState.rollingBack)
        }

        // Rollback step calls
        Mockito.`when`(kitchenServiceProxyMock.rejectTicket(sagaState.sagaId)).thenReturn(true)

        for(i in 3..5){
            val retval = sagaDefinition.step()
            assertEquals(SagaStepResult.UNFINISHED, retval)
            assertTrue(sagaState.rollingBack)
        }

        assertEquals(SagaStepResult.ROLLED_BACK, sagaDefinition.step())
        assertTrue(sagaState.rollingBack)

        val createdOrder = orderRepository.readOrderForSagaId(sagaState.sagaId)
        assertNotNull(createdOrder)
        assertEquals(EntityStates.CANCELED, createdOrder.orderState)
    }

}