package com.oli.address

import com.oli.customer.CustomerService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureAddressRouting() {
    val service by inject<CustomerService>()

    routing {
        put("/address") {
            val address = call.receive<Address>()
            val result = service.updateAddress(address)
            if (result) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }

}
