package com.oli.customer

import com.oli.address.Address
import com.oli.address.AddressDAO
import com.oli.event.*
import org.slf4j.Logger

class CustomerService(
    private val customerDAO: CustomerDAO,
    private val addressDAO: AddressDAO,
    private val logger: Logger
) {
    private val createOrderSagaReplyChannelName =
        System.getenv("CREATE_ORDER_SAGA_REPLY_CHANNEL") ?: "create_order_saga_reply_channel"

    /**
     * Create customer.
     * @param customer The customer to be created.
     * @return ID of the created customer
     */
    suspend fun create(customer: Customer): Int? {
        return customerDAO.create(customer)?.id
    }

    /**
     * Verify that a customer exists and that the provided address is valid.
     * @param customerId The id of the customer to be checked.
     * @param address The address to be verified.
     * @return True if the provided address is an address that matches any of the addresses of the customer.
     */
    suspend fun verify(customerId: Int, address: Address): Boolean? {
        val storedCustomer: Customer = read(customerId) ?: return null
        return storedCustomer.addresses.any { it.equalsIgnoreId(address) }
    }

    /**
     * Read customer.
     * @param id ID of the customer to be read.
     * @return Customer or null if not found.
     */
    suspend fun read(id: Int): Customer? {
        return customerDAO.read(id)
    }

    /**
     * Update a customer (addresses ignored).
     * @param id ID of the customer to be updated.
     * @param customer Customer to be updated.
     */
    suspend fun update(customer: CustomerNoAddress): Int {
        return customerDAO.update(customer)
    }

    /**
     * Update a address.
     * @param address The address to be updated.
     * @return The amount of affected entries.
     */
    suspend fun updateAddress(address: Address): Boolean {
        return addressDAO.update(address) >= 1
    }

    /**
     * Delete a customer and all associated addresses.
     * @param id ID of the customer to be deleted.
     * @return The amount of affected entries.
     */
    suspend fun delete(id: Int): Int {
        return customerDAO.delete(id)
    }

    suspend fun handleEvent(correlationId: String, event: Event): Event {
        logger.debug("Received event $event")
        return when (event) {
            is VerifyCustomerCommandEvent -> handleCustomerVerificationEvent(correlationId, event)
            else -> handleUnknownEvent(correlationId, event)
        }
    }

    private suspend fun handleCustomerVerificationEvent(correlationId: String, event: VerifyCustomerCommandEvent): ReplyEvent {
        val result = verify(event.customerId, event.address) ?: false
        val response = ReplyEvent(event.sagaId, result)
        logger.debug("CorrelationId: $correlationId. Customer verification for completed.")
        return response
    }

    private fun handleUnknownEvent(correlationId: String, event: Event): ErrorEvent {
        val msg = "Correlation id: $correlationId. Received unknown event type. Event: $event"
        logger.debug(msg)
        return ErrorEvent(msg)
    }
}
