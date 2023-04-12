package com.oli.saga

import kotlin.test.Test
import kotlin.test.assertEquals

class SagaDSLTest {

    @Test
    fun buildSagaTest() {
        val descriptionFirstStep = "1. example step"
        val stepFirstStep: suspend () -> StepResult = { StepResult.SUCCESS }
        val compensationFirstStep: suspend () -> StepResult = { StepResult.SUCCESS }
        val onResultFirstStep: suspend () -> Unit = {}

        val descriptionSecondStep = "2. example step"
        val stepSecondStep: suspend () -> StepResult = { StepResult.FAILURE }
        val compensationSecondStep: suspend () -> StepResult = { StepResult.FAILURE }


        val saga = saga {
            step {
                description = descriptionFirstStep
                transaction = stepFirstStep
                compensatingTransaction = compensationFirstStep
                onResult = onResultFirstStep
            }
            step {
                description = descriptionSecondStep
                transaction = stepSecondStep
                compensatingTransaction = compensationSecondStep
            }
        }

        assertEquals(2, saga.steps.size)

        assertEquals(descriptionFirstStep, saga.steps[0].description)
        assertEquals(stepFirstStep, saga.steps[0].transaction)
        assertEquals(compensationFirstStep, saga.steps[0].compensatingTransaction)
        assertEquals(onResultFirstStep, saga.steps[0].onResult)

        assertEquals(descriptionSecondStep, saga.steps[1].description)
        assertEquals(stepSecondStep, saga.steps[1].transaction)
        assertEquals(compensationSecondStep, saga.steps[1].compensatingTransaction)
    }

}