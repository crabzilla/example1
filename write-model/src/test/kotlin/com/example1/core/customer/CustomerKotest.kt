package com.example1.core.customer

import io.kotest.core.spec.style.BehaviorSpec
import org.assertj.core.api.Assertions.assertThat
import java.util.UUID

class CustomerKotest : BehaviorSpec({

    var state: Customer
    given("An inactive customer") {
        val customerId = UUID.randomUUID()
        val name = "Bob"
        state = Customer.create(id = customerId, name = name).state
        println(state)
        When("CustomerActivated event occurs") {
            val reason = "because I need it"
            val event2 = CustomerEvent.CustomerActivated(reason)
            val state2 = customerEventHandler.handleEvent(state, event2)
            then("the customer is now active") {
                assertThat(state2).isEqualTo(Customer(id = customerId, name = name, reason = reason, isActive = true))
                state = state2
            }
        }
        println(state)
        When("CustomerDeactivated event occurs") {
            val reason = "because I need it again"
            val event3 = CustomerEvent.CustomerDeactivated(reason)
            val state3 = customerEventHandler.handleEvent(state, event3)
            then("the customer is now inactive") {
                assertThat(state3).isEqualTo(Customer(id = customerId, name = name, reason = reason, isActive = false))
                state = state3
            }
        }
        println(state)
    }


})