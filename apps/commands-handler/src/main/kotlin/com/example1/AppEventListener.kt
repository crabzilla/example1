package com.example1

import com.example1.core.customer.Customer
import com.example1.core.customer.CustomerCommand
import com.example1.core.customer.CustomerEvent
import com.example1.customer.CustomerVerticle
import com.example1.infra.registerLocalCodec
import io.github.crabzilla.stack.CommandController
import io.micronaut.context.event.ShutdownEvent
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.pgclient.PgPool
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AppEventListener {

    companion object {
        private val log = LoggerFactory.getLogger(AppEventListener::class.java)
    }

    @Inject
    lateinit var vertx: Vertx
    @Inject
    lateinit var controller: CommandController<Customer, CustomerCommand, CustomerEvent>
    @Named("writeDb")
    lateinit var writeDb: PgPool

    @EventListener
    internal fun onStartupEvent(event: StartupEvent) {
        vertx.registerLocalCodec()
        val deploymentOptions = DeploymentOptions().setHa(false)
        (1..4)
            .asSequence()
            .map { CustomerVerticle() }
            .toList()
            .onEach { verticle ->
                verticle.controller = controller
                vertx.deployVerticle(verticle, deploymentOptions)
                    .onSuccess { log.info("Successfully started $it") }
                    .onFailure { log.error("When starting", it) }
            }
    }

    @EventListener
    internal fun onShutdownEvent(event: ShutdownEvent) {
        vertx.close()
        writeDb.close()
    }

}