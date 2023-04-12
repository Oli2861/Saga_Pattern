package com.oli.saga

import org.koin.java.KoinJavaComponent.inject
import org.slf4j.Logger

enum class SagaStepResult { UNFINISHED, FINISHED, ROLLED_BACK }

abstract class SagaDefinition(
    val sagaState: SagaState,
    private val sagaName: String
) {
    protected abstract val saga: Saga
    private val logger: Logger by inject(Logger::class.java)

    /**
     * Proceed in the saga by one step.
     * @return CreateOrderSagaResult
     */
    suspend fun step(): SagaStepResult {
        val stepFunction = saga.steps[sagaState.currentStep]
        val result = if (sagaState.rollingBack) {
            rollbackStep(stepFunction)
        } else {
            step(stepFunction)
        }
        return result
    }

    private suspend fun rollbackStep(step: Step): SagaStepResult {
        val stepResult = step.compensatingTransaction?.invoke() ?: StepResult.SUCCESS

        if (stepResult == StepResult.SUCCESS) {
            logger.debug("$sagaName ${sagaState.sagaId} rolled back to step ${sagaState.currentStep} with description: ${step.description}")
            sagaState.currentStep -= 1
        } else {
            // TODO: Prevent infinite retries
            logger.debug("$sagaName ${sagaState.sagaId} could not be rolled back to step ${sagaState.currentStep} with description: ${step.description} The rollback will be tried again.")
        }

        return if (sagaState.currentStep == -1) SagaStepResult.ROLLED_BACK
        else SagaStepResult.UNFINISHED
    }

    private suspend fun step(step: Step): SagaStepResult {
        val stepResult = step.transaction?.invoke() ?: StepResult.SUCCESS
        when (stepResult) {
            StepResult.SUCCESS -> {
                logger.debug("$sagaName ${sagaState.sagaId} proceeded to step ${sagaState.currentStep} with description: ${step.description}")
                sagaState.currentStep += 1
            }
            StepResult.FAILURE -> {
                logger.debug("$sagaName ${sagaState.sagaId} proceeded to step ${sagaState.currentStep} with description: ${step.description} The result was negative, the saga will be rolled back.")
                sagaState.rollingBack = true
                sagaState.currentStep -= 1
            }
            StepResult.RETRY -> {
                // TODO: Prevent infinite retries
                logger.debug("$sagaName ${sagaState.sagaId} proceeded to step ${sagaState.currentStep} with description: ${step.description} The result was negative, the saga will retry the current step.")
            }
        }

        return if (sagaState.currentStep == saga.steps.size) SagaStepResult.FINISHED
        else SagaStepResult.UNFINISHED
    }
}
