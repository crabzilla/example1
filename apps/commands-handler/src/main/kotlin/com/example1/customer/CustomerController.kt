package com.example1.customer

import com.example1.core.customer.Customer
import com.example1.core.customer.CustomerCommand
import com.example1.core.customer.CustomerEvent
import com.example1.customer.CustomerVerticle.Companion.CUSTOMER_COMMAND_ENDPOINT
import com.example1.infra.localRequest
import io.github.crabzilla.core.StatefulSession
import io.github.crabzilla.stack.CommandMetadata
import io.micronaut.context.annotation.Context
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.reactivex.Single
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

@Controller("/hello")
@Context
class CustomerController(private val vertx: Vertx) {

    companion object {
        private val log = LoggerFactory.getLogger(CustomerController::class.java)
    }

    val id = AtomicInteger(0)

    @Get("/")
    fun index(): Single<StatefulSession.SessionData> {
        val newId = id.incrementAndGet()
        if (log.isDebugEnabled) log.debug("*** Will generate a new command $newId")
        val metadata = CommandMetadata(newId)
        val command = CustomerCommand.RegisterCustomer(newId, "customer#$newId")
        return Single.create { emitter ->
            val  deliveryOptions = DeliveryOptions().setLocalOnly(true) // .setSendTimeout(45_000) // to make ab test happy
            vertx.eventBus().localRequest<StatefulSession<Customer, CustomerEvent>>(
                CUSTOMER_COMMAND_ENDPOINT, Pair(metadata, command), deliveryOptions) {
                if (it.failed()) {
                    emitter.onError(it.cause())
                    log.error("Error", it.cause())
                    return@localRequest
                }
                val result = it.result().body().toSessionData()
                if (log.isDebugEnabled) log.debug("Success: $result")
                emitter.onSuccess(result)
            }
        }
    }

}
