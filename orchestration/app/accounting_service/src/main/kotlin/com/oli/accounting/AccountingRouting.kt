package com.oli.accounting

import com.oli.creditcardinfo.CreditCardInfo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.accountingRouting() {
    val service by inject<AccountingService>()

    routing {
        post("/creditcardinfo") {
            val creditCardInfo = call.receive<CreditCardInfo>()
            val result = service.addCreditCardInfo(creditCardInfo)
            if (result == null) {
                call.respond(HttpStatusCode.Conflict)
            } else {
               call.respond(HttpStatusCode.Created, result)
            }
        }
    }
}
