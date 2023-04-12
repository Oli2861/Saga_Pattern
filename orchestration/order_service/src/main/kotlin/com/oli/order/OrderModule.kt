package com.oli.order

import com.oli.event.MessageBroker
import com.oli.event.RabbitMQBroker
import com.oli.persistence.CreateOrderSagaStateDAOImpl
import com.oli.persistence.OrderDAOImpl
import com.oli.persistence.OrderSagaAssociationDAOImpl
import com.oli.proxies.OrderServiceProxy
import com.oli.saga.CreateOrderSagaStateDAO
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.dsl.module
import org.koin.ktor.plugin.koin

fun Application.orderModule() {
    koin {
        configureOrderRouting()
    }
}

fun Application.configureOrderRouting() {
    routing {
        orderRouting()
    }
}

private val orderKoinModule = module {

    single<OrderDAO> {
        OrderDAOImpl()
    }

    single<OrderSagaAssociationDAO> {
        OrderSagaAssociationDAOImpl()
    }

    single<CreateOrderSagaStateDAO> {
        CreateOrderSagaStateDAOImpl()
    }

    single<OrderRepository> {
        OrderRepositoryImpl(
            orderDAO = get(),
            orderSagaAssociationDAO = get()
        )
    }

    single<MessageBroker> {
        RabbitMQBroker
    }

    single<OrderService> {
        OrderService(
            orderRepository = get(),
            createOrderSagaManager = get(),
            logger = get()
        )
    }
    single<OrderServiceProxy> { OrderService(orderRepository = get(), logger = get(), createOrderSagaManager = get()) }

}