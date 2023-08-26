package com.oli.order

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

object OrderRoutingConstants {
    const val ORDER_ROUTE_PATH = "/order"
}

fun Route.orderRouting() {
    val orderService by inject<OrderService>()

    route(OrderRoutingConstants.ORDER_ROUTE_PATH) {

        post {
            val order = call.receive<Order>()
            val result = orderService.createOrderSaga(order)
            return@post if (result == null || (!result.second)) {
                call.respondText("Unable to create the order", status = HttpStatusCode.Conflict)
            } else {
                call.respondText("Created order with orderID ${result.first}", status = HttpStatusCode.Created)
            }
        }

        delete("{id}") {
            val id: String = call.parameters["id"] ?: return@delete call.respondText(
                "Missing orderId",
                status = HttpStatusCode.BadRequest
            )
            val success: Boolean = orderService.deleteOrder(id) == 1

            return@delete if (success) {
                call.respondText("Order with orderID $id is deleted.", status = HttpStatusCode.OK)
            } else {
                call.respondText("Unable to delete order with orderID $id", status = HttpStatusCode.NotFound)
            }
        }

        get("{id}") {
            val id: String = call.parameters["id"] ?: return@get call.respondText(
                "Missing orderId",
                status = HttpStatusCode.BadRequest
            )
            val order = orderService.readOrder(id)

            return@get if (order == null) {
                call.respondText("Unable to retrieve order with id $id", status = HttpStatusCode.NotFound)
            } else {
                call.respond(order)
            }
        }
    }
}
