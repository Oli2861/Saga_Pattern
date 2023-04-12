package com.oli.saga

interface SagaState{
    val sagaId: Int
    var currentStep: Int
    var rollingBack: Boolean
}
