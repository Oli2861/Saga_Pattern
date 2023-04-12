package com.oli

import com.oli.persistence.DatabaseFactory
import com.oli.ticket.OrderItem
import com.oli.ticket.Ticket
import com.oli.ticket.TicketDAO
import com.oli.ticket.TicketDAOImpl
import kotlinx.coroutines.runBlocking
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TicketDAOTest {
    companion object {
        lateinit var ticketDAO: TicketDAO

        @JvmStatic
        @BeforeClass
        fun init() {
            DatabaseFactory.init(true)
            ticketDAO = TicketDAOImpl()
        }
    }

    @Test
    fun createTest() = runBlocking {
        val ticket = Ticket(0, 1, 2, 1, listOf(OrderItem(0, 10, 2), OrderItem(0, 20, 3)))
        val created = ticketDAO.create(ticket)
        assertTrue(ticket.equalsIgnoreTicketId(created))
    }

    @Test
    fun readTest() = runBlocking{
        val ticket = Ticket(0, 1, 2, 1, listOf(OrderItem(0, 10, 2), OrderItem(0, 20, 3)))
        val created = ticketDAO.create(ticket)
        val read = ticketDAO.read(created!!.id)

        assertTrue(ticket.equalsIgnoreTicketId(read))
    }

    @Test
    fun updateStateTest() = runBlocking{
        val ticket = Ticket(0, 1, 2, 1, listOf(OrderItem(0, 10, 2), OrderItem(0, 20, 3)))
        val created = ticketDAO.create(ticket)
        val updated = ticketDAO.updateState(ticket.sagaId, 0)
        val read = ticketDAO.read(created!!.id)

        assertEquals(1, updated)
        assertEquals(0, read!!.state)
    }

}