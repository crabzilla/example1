package com.example1.customers

import io.github.crabzilla.core.StatefulSession
import io.github.crabzilla.example1.Customer
import io.github.crabzilla.example1.CustomerCommand
import io.github.crabzilla.example1.CustomerEvent
import io.github.crabzilla.stack.CommandController
import io.github.crabzilla.stack.CommandMetadata
import io.micronaut.context.annotation.Context
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.reactivex.Single
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Named

@Controller("/hello")
@Context
class TestController(@Named("cassandra")
                     private val controller: CommandController<Customer, CustomerCommand, CustomerEvent>) {

    companion object {
        private val log = LoggerFactory.getLogger(TestController::class.java)
    }

    val id = AtomicInteger()

    @Get("/")
    fun index(): Single<StatefulSession.SessionData> {
        val newId = id.incrementAndGet()
//        val newId = 100003
        if (log.isDebugEnabled) log.debug("*** Will generate a new command $newId")
        val metadata = CommandMetadata(newId)
        val command = CustomerCommand.RegisterCustomer(newId, "customer#$newId")
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
