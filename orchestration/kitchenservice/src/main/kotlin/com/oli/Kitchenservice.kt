package com.oli

import com.oli.event.*
import com.oli.ticket.EntityStates
import com.oli.ticket.Ticket
import com.oli.ticket.TicketDAO
import org.slf4j.Logger

class KitchenService(
    private val logger: Logger,
    private val ticketDAO: TicketDAO
) {

    suspend fun handleEvent(correlationId: String, event: Event): Event {
        logger.debug("Received event $event")
        return when (event) {
            is CreateTicketCommandEvent -> handleCreateTicketCommandEvent(correlationId, event)
            is ApproveTicketCommandEvent -> handleApproveTicketCommandEvent(correlationId, event)
            is RejectTicketCommandEvent -> handleRejectTicketCommandEvent(correlationId, event)
            else -> handleUnknownEvent(correlationId, event)
        }
    }

    private suspend fun handleApproveTicketCommandEvent(correlationId: String, event: ApproveTicketCommandEvent): ReplyEvent {
        val result = ticketDAO.updateState(event.sagaId, EntityStates.APPROVED)
        val replyEvent = ReplyEvent(event.sagaId, result >= 1)
        logger.debug("Correlation id: $correlationId. Handled approve ticket command, modified $result entries.")
        return replyEvent
    }

    private suspend fun handleRejectTicketCommandEvent(correlationId: String, event: RejectTicketCommandEvent): ReplyEvent {
        val result = ticketDAO.updateState(event.sagaId, EntityStates.CANCELED)
        val replyEvent = ReplyEvent(event.sagaId, result >= 1)
        logger.debug("Correlation id: $correlationId. Handled reject ticket command, modified $result entries.")
        return replyEvent
    }

    private suspend fun handleCreateTicketCommandEvent(correlationId: String, event: CreateTicketCommandEvent): ReplyEvent {
        val ticket = Ticket(0, event.customerId, event.sagaId, EntityStates.PENDING, event.orderItems)
        val createdTicketId = ticketDAO.create(ticket)?.id
        val replyEvent = ReplyEvent(event.sagaId, createdTicketId != null)
        logger.debug("CorrelationId: $correlationId. Created ticket with id $createdTicketId")
        return replyEvent
    }

    private fun handleUnknownEvent(correlationId: String, event: Event): ErrorEvent {
        val msg = "Correlation id: $correlationId. Received unknown event type. Event: $event"
        logger.debug(msg)
        return ErrorEvent(msg)
    }

}