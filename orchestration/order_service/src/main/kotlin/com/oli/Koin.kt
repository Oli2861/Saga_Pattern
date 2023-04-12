package com.oli

import com.oli.event.MessageBroker
import com.oli.event.RabbitMQBroker
import com.oli.order.*
import com.oli.persistence.CreateOrderSagaStateDAOImpl
import com.oli.persistence.OrderDAOImpl
import com.oli.persistence.OrderSagaAssociationDAOImpl
import com.oli.proxies.*
import com.oli.saga.CreateOrderSagaManager
import com.oli.saga.CreateOrderSagaStateDAO
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(appKoinModule)
    }
}

private val appKoinModule = module {
    single{ LoggerFactory.getLogger("") }
    single<MessageBroker> { RabbitMQBroker }
    single<OrderDAO> { OrderDAOImpl() }
    single<OrderSagaAssociationDAO> { OrderSagaAssociationDAOImpl() }
    single<CreateOrderSagaStateDAO> { CreateOrderSagaStateDAOImpl() }
    single<CustomerServiceProxy> { CustomerServiceProxyImpl(messageBroker = get(), logger = get()) }
    single<KitchenServiceProxy> { KitchenServiceProxyImpl(messageBroker = get(), logger = get()) }
    single<AccountingServiceProxy> { AccountingServiceProxyImpl(messageBroker = get(), logger = get()) }
    single {
        CreateOrderSagaManager(
            orderSagaStateDAO = get(),
            logger = get()
        )
    }
    single<OrderRepository> { OrderRepositoryImpl(orderDAO = get(), orderSagaAssociationDAO = get()) }
    single<OrderService> { OrderService(orderRepository = get(), logger = get(), createOrderSagaManager = get()) }
    single<OrderServiceProxy> { get<OrderService>() }
}