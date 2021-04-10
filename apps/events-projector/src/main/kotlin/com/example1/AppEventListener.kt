package com.example1

import com.example1.customers.CustomerProjectionVerticle
import com.example1.infra.registerLocalCodec
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
    lateinit var customerProjectionVerticle: CustomerProjectionVerticle
    @Named("writeDb")
    lateinit var writeDb: PgPool
    @Named("readDb")
    lateinit var readDb: PgPool

    @EventListener
    internal fun onStartupEvent(event: StartupEvent) {
        vertx.registerLocalCodec()
        val deploymentOptions = DeploymentOptions().setHa(false).setInstances(1)
        vertx.deployVerticle(customerProjectionVerticle, deploymentOptions)
            .onSuccess { log.info("Successfully started $it") }
            .onFailure { log.error("When starting", it) }
    }

    @EventListener
    internal fun onShutdownEvent(event: ShutdownEvent) {
        vertx.close()
        writeDb.close()
        readDb.close()
    }

}