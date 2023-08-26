package com.oli.saga

import com.oli.order.Order
import org.slf4j.Logger


class CreateOrderSagaManager(
    private val orderSagaStateDAO: CreateOrderSagaStateDAO,
    val logger: Logger
) {

    suspend fun createAndExecuteSaga(order: Order): Pair<Boolean, Int>? {
        val sagaState = orderSagaStateDAO.create(CreateOrderSagaState(0, 0, false, null))
        return if (sagaState == null) {
            logger.debug("Error while creating saga definition: The created saga state is null.")
            null
        } else {
            val sagaDefinition = CreateOrderSagaDefinition(sagaState, order)
            executeSaga(sagaDefinition)
        }
    }

    private suspend fun executeSaga(sagaDefinition: CreateOrderSagaDefinition): Pair<Boolean, Int> {
        var result: SagaStepResult = SagaStepResult.UNFINISHED
        while (result == SagaStepResult.UNFINISHED) {
            result = sagaDefinition.step()
        }
        return when (result) {
            SagaStepResult.ROLLED_BACK -> {
                logger.debug("${sagaDefinition.sagaState.sagaId} rolled back.")
                Pair(false, sagaDefinition.sagaState.sagaId)
            }

            SagaStepResult.FINISHED -> {
                logger.debug("${sagaDefinition.sagaState.sagaId} completed successfully.")
                Pair(true, sagaDefinition.sagaState.sagaId)
            }

            else -> {
                logger.debug("${sagaDefinition.sagaState.sagaId} unexpected SagaStepResult.")
                Pair(false, sagaDefinition.sagaState.sagaId)
            }
        }
    }

}