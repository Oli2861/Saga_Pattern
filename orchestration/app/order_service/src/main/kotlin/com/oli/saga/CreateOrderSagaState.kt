package com.oli.saga

import com.oli.order.Orders
import org.jetbrains.exposed.dao.id.IntIdTable


data class CreateOrderSagaState(
    override val sagaId: Int,
    override var currentStep: Int,
    override var rollingBack: Boolean,
    var orderId: Int? = null
) : SagaState {

    fun equalsIgnoreId(other: Any?): Boolean {
        if (other !is CreateOrderSagaState) return false
        if(other.currentStep != currentStep) return false
        if(other.rollingBack != rollingBack) return false
        if(other.orderId != orderId) return false
        return true
    }
}

object CreateOrderSagaStates : IntIdTable() {
    var currentStep = integer("currentStep")
    val rollingBack = bool("rollingBack")

    val orderId = reference("orderDetailsId", Orders.id).nullable()
}