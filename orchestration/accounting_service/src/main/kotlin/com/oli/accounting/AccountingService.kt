package com.oli.accounting

import com.oli.creditcardinfo.CreditCardInfo
import com.oli.creditcardinfo.CreditCardInfoDAO
import com.oli.event.AuthorizationCommandEvent
import com.oli.event.ErrorEvent
import com.oli.event.Event
import com.oli.event.ReplyEvent
import org.slf4j.Logger

class AccountingService(
    private val logger: Logger,
    private val creditCardInfoDAO: CreditCardInfoDAO
) {

    suspend fun addCreditCardInfo(creditCardInfo: CreditCardInfo): Int? {
        return creditCardInfoDAO.create(creditCardInfo)?.userId
    }

    suspend fun handleEvent(correlationId: String, event: Event): Event {
        logger.debug("Received event $event")
        return when (event) {
            is AuthorizationCommandEvent -> handleCustomerVerificationEvent(correlationId, event)
            else -> handleUnknownEvent(correlationId, event)
        }
    }

    private suspend fun handleCustomerVerificationEvent(correlationId: String, commandEvent: AuthorizationCommandEvent): ReplyEvent {
        val entryList = creditCardInfoDAO.get(commandEvent.userId)
        val result = entryList.any{ it.info == commandEvent.paymentInfo}
        logger.debug("Correlation id: $correlationId. Payment info verification completed with ${if(result) "positive" else "negative"} result.")
        return ReplyEvent(commandEvent.sagaId, result)
    }

    private fun handleUnknownEvent(correlationId: String, event: Event): ErrorEvent {
        val msg = "Correlation id: $correlationId. Received unknown event type. Event: $event"
        logger.debug(msg)
        return ErrorEvent(msg)
    }
}