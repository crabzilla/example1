package com.example1

import com.example1.projections.customers.CustomerProjectorVerticle
import com.example1.infra.registerLocalCodec
import io.github.crabzilla.stack.PoolingProjectionVerticle
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

    @Inject  @field:Named("nats")
    lateinit var natsProjectionVerticle: PoolingProjectionVerticle

    @Inject  @field:Named("customers")
    lateinit var customersProjectionVerticle: PoolingProjectionVerticle

    @Inject
    lateinit var customerProjectorVerticle: CustomerProjectorVerticle

    @Named("writeDb")
    lateinit var writeDb: PgPool

    @Named("readDb")
    lateinit var readDb: PgPool

    @EventListener
    internal fun onStartupEvent(event: StartupEvent) {
        vertx.registerLocalCodec()
        val deploymentOptions = DeploymentOptions().setHa(false).setInstances(1)
        vertx.deployVerticle(natsProjectionVerticle, deploymentOptions)
            .compose { vertx.deployVerticle(customersProjectionVerticle, deploymentOptions) }
            .compose { vertx.deployVerticle(customerProjectorVerticle, deploymentOptions) }
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