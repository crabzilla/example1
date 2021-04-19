package com.example1.customers

import com.example1.customers.CommandsController.CustomerRequest.*
import io.github.crabzilla.core.StatefulSession
import io.github.crabzilla.example1.Customer
import io.github.crabzilla.example1.CustomerCommand
import io.github.crabzilla.example1.CustomerCommand.*
import io.github.crabzilla.example1.CustomerEvent
import io.github.crabzilla.stack.CommandController
import io.github.crabzilla.stack.CommandMetadata
import io.micronaut.context.annotation.Context
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Put
import io.reactivex.Single
import org.slf4j.LoggerFactory
import javax.inject.Named
import javax.inject.Singleton

@Controller("/customers")
@Singleton
class CommandsController(@Named("postgress")
                         private val controller: CommandController<Customer, CustomerCommand, CustomerEvent>) {

    companion object {
        private val log = LoggerFactory.getLogger(CommandsController::class.java)
    }

    @Introspected
    sealed class CustomerRequest {
        @Introspected
        data class RegisterRequest(val name: String): CustomerRequest()
        @Introspected
        data class ActivateRequest(val reason: String): CustomerRequest()
        @Introspected
        data class DeactivateRequest(val reason: String): CustomerRequest()
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Put("/{id}")
    fun handle(@PathVariable id: Int, @Body req: CustomerRequest): Single<StatefulSession.SessionData> {
        log.info("Received $id / $req")
        val metadata = CommandMetadata(id)
        val command = when (req) {
            is RegisterRequest -> RegisterCustomer(id, req.name)
            is ActivateRequest -> ActivateCustomer(req.reason)
            is DeactivateRequest -> DeactivateCustomer(req.reason)
        }
        return Single.create { emitter ->
            controller.handle(metadata, command)
                .onFailure {
                    log.error("Error", it)
                    emitter.onError(it)
                }
                .onSuccess { session: StatefulSession<Customer, CustomerEvent> ->
                    if (log.isDebugEnabled) log.debug("Result: ${session.toSessionData()}")
                    emitter.onSuccess(session.toSessionData())
                }
        }
    }

}
