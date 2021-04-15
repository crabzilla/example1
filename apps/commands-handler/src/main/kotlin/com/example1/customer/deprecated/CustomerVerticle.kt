package com.example1.customer.deprecated

import com.example1.infra.localReply
import io.github.crabzilla.core.Command
import io.github.crabzilla.core.StatefulSession
import io.github.crabzilla.example1.Customer
import io.github.crabzilla.example1.CustomerCommand
import io.github.crabzilla.example1.CustomerEvent
import io.github.crabzilla.stack.CommandController
import io.github.crabzilla.stack.CommandMetadata
import io.vertx.core.AbstractVerticle
import org.slf4j.LoggerFactory

class CustomerVerticle : AbstractVerticle() {

    companion object {
        private val log = LoggerFactory.getLogger(CustomerVerticle::class.java)
        const val CUSTOMER_COMMAND_ENDPOINT = "customer-command-handler";
    }

    lateinit var controller: CommandController<Customer, CustomerCommand, CustomerEvent>

    override fun start() {
        vertx.eventBus().localConsumer<Pair<CommandMetadata, Command>>(CUSTOMER_COMMAND_ENDPOINT) { message ->
            if (log.isDebugEnabled) log.debug("Received ${message.body()}")
            controller.handle(message.body().first, message.body().second as CustomerCommand)
                .onFailure {
                    message.fail(500, it.message ?: "I'm sorry")
                }
                .onSuccess { result: StatefulSession<Customer, CustomerEvent> ->
                    message.localReply(result)
                }
        }
    }

}