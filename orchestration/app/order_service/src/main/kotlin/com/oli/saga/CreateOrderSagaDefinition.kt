package com.oli.saga

import com.oli.order.Order
import com.oli.proxies.AccountingServiceProxy
import com.oli.proxies.CustomerServiceProxy
import com.oli.proxies.KitchenServiceProxy
import com.oli.proxies.OrderServiceProxy
import org.koin.java.KoinJavaComponent.inject

/**
 * Saga to create an order.
 * The saga consists of multiple steps which include (forward) transactions and compensating transactions.
 * @param orderDetails Details of the order such as which articles are ordered
 * @param orderSagaState State of the saga.
 * @param orderService Order service to create orders.
 * @param consumerServiceProxy Proxy class of the consumer service used to make RPC calls.
 * @param kitchenServiceProxy Proxy class of the kitchen service used to make RPC calls.
 * @param accountingServiceProxy Proxy class of the accounting service used to make RPC calls.
 * @property saga Definition of the saga steps including transactions and compensating transactions.
 */
class CreateOrderSagaDefinition(
    private val orderSagaState: CreateOrderSagaState,
    private var order: Order
) : SagaDefinition(orderSagaState, CreateOrderSagaDefinition::class.java.name) {
    private val orderServiceProxy: OrderServiceProxy by inject(OrderServiceProxy::class.java)
    private val consumerServiceProxy: CustomerServiceProxy by inject(CustomerServiceProxy::class.java)
    private val kitchenServiceProxy: KitchenServiceProxy by inject(KitchenServiceProxy::class.java)
    private val accountingServiceProxy: AccountingServiceProxy by inject(AccountingServiceProxy::class.java)

    override val saga: Saga = saga {
        step {
            description = "Step 1: Create an order in the pending state. Compensate by setting its state to rejected."
            transaction = ::orderServiceCreateOrder
            compensatingTransaction = ::orderServiceRejectOrder
        }
        step {
            description = "Step 2: Verify the customer details by calling the customer service."
            transaction = ::consumerServiceVerifyConsumerDetails
        }
        step {
            description =
                "Step 3: Create a ticket in the kitchen service in the pending state. Compensate by setting its state to rejected."
            transaction = ::kitchenServiceCreateTicket
            compensatingTransaction = ::kitchenServiceCancelTicket
        }
        step {
            description = "Step 4: Authorize the payment in the accounting service. This is a pivot transaction."
            transaction = ::accountingServiceAuthorize
        }
        step {
            description = "Step 5: Approve the ticket in the kitchen service by settings its state to approved."
            transaction = ::kitchenServiceApproveTicket
        }
        step {
            description = "Step 6: Approve the ticket in the order service by setting its state to approved."
            transaction = ::orderServiceApproveTicket
        }
    }

    /**
     * Create an order.
     * @return Whether the order was created successfully.
     */
    private suspend fun orderServiceCreateOrder(): StepResult {
        if (order.id != 0 || order.orderState != EntityStates.PENDING) {
            order = Order(
                customerId = order.customerId,
                address = order.address,
                paymentInfo = order.paymentInfo,
                timestamp = order.timestamp,
                items = order.items
            )
        }
        val (createdOrder, createdAssociation) = orderServiceProxy.createOrder(orderSagaState.sagaId, order)
        return if (createdOrder == null || createdAssociation == null) {
            StepResult.FAILURE
        } else {
            // Update the received order used in this saga to the created one.
            order = createdOrder
            // Set the orderId of the saga state, as the order was now persisted.
            orderSagaState.orderId = order.id
            StepResult.SUCCESS
        }
    }

    /**
     * Compensate an order creation by setting its state to canceled.
     */
    private suspend fun orderServiceRejectOrder(): StepResult {
        val affectedEntries = orderServiceProxy.updateOrderState(orderSagaState.sagaId, EntityStates.CANCELED)
        return if (affectedEntries) StepResult.SUCCESS else StepResult.RETRY
    }

    private suspend fun consumerServiceVerifyConsumerDetails(): StepResult {
        return if (consumerServiceProxy.sendVerifyCustomerDetailsCommand(
                orderSagaState.sagaId,
                order.customerId,
                order.address
            )
        ) StepResult.SUCCESS else StepResult.FAILURE
    }

    private suspend fun kitchenServiceCreateTicket(): StepResult {
        return if (kitchenServiceProxy.createTicket(
                orderSagaState.sagaId,
                order.customerId,
                order.items
            )
        ) StepResult.SUCCESS else StepResult.FAILURE
    }

    private suspend fun kitchenServiceCancelTicket(): StepResult {
        val affectedEntries = kitchenServiceProxy.rejectTicket(orderSagaState.sagaId)
        return if (affectedEntries) StepResult.SUCCESS else StepResult.RETRY
    }

    private suspend fun accountingServiceAuthorize(): StepResult {
        return if (accountingServiceProxy.authorize(
                orderSagaState.sagaId,
                order.customerId,
                order.paymentInfo
            )
        ) StepResult.SUCCESS else StepResult.FAILURE
    }

    private suspend fun kitchenServiceApproveTicket(): StepResult {
        val affectedEntries = kitchenServiceProxy.approveTicket(orderSagaState.sagaId)
        return if (affectedEntries) StepResult.SUCCESS else StepResult.RETRY
    }

    private suspend fun orderServiceApproveTicket(): StepResult {
        val affectedEntries = orderServiceProxy.updateOrderState(orderSagaState.sagaId, EntityStates.APPROVED)
        return if (affectedEntries) StepResult.SUCCESS else StepResult.RETRY
    }
}

