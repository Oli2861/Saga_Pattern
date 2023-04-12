package com.oli.customer

import com.oli.address.Address
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureCustomerRouting() {
    val service by inject<CustomerService>()

    routing {

        post("/users") {
            val user = call.receive<Customer>()
            val id = service.create(user)
            if(id != null){
                call.respond(HttpStatusCode.Created, id)
            }else{
                call.respond(HttpStatusCode.Conflict)
            }
        }

        get("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = service.read(id)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/users/verify/{id}"){
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val address = call.receive<Address>()
            val isOkay = service.verify(id, address)
            if (isOkay != null) {
                call.respond(HttpStatusCode.OK, isOkay)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        put("/users") {
            val customer = call.receive<CustomerNoAddress>()
            service.update(customer)
            call.respond(HttpStatusCode.OK)
        }

        delete("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            service.delete(id)
            call.respond(HttpStatusCode.OK)
        }

    }
}