package com.oli.customer

import com.oli.address.AddressDAO
import com.oli.address.AddressDAOImpl
import com.oli.event.MessageBroker
import com.oli.event.RabbitMQBroker
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.koin
import org.slf4j.LoggerFactory

fun Application.customerModule() {
    koin {
        modules(customerKoinModule)
        configureCustomerRouting()
    }
}

private val customerKoinModule = module {

    single<MessageBroker> {
        RabbitMQBroker
    }

    single<CustomerService> {
        CustomerService(
            customerDAO = get(),
            addressDAO = get(),
            logger = get()
        )

    }

    single<CustomerDAO> {
        CustomerDAOImpl()
    }

    single<AddressDAO> {
        AddressDAOImpl()
    }

}