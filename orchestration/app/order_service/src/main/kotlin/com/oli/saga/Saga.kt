package com.oli.saga

/**
 * Saga function for DSL.
 */
fun saga(function: Saga.() -> Unit): Saga {
    val saga = Saga()
    saga.function()
    return saga
}


data class Saga(val steps: MutableList<Step> = mutableListOf()) {
    /**
     * Step function for DSL.
     */
    fun step(function: Step.() -> Unit) {
        val step = Step()
        step.function()
        steps.add(step)
    }
}

data class Step(
    var description: String? = null,
    var transaction: (suspend () -> StepResult)? = null,
    var compensatingTransaction: (suspend () -> StepResult)? = null,
    var onResult: (suspend () -> Unit)? = null
)

enum class StepResult{
    SUCCESS, RETRY, FAILURE
}