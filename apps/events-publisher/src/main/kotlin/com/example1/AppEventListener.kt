package com.example1

import com.example1.infra.registerLocalCodec
import io.github.crabzilla.stack.PoolingProjectionVerticle
import io.micronaut.context.event.ShutdownEvent
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppEventListener {

    companion object {
        private val log = LoggerFactory.getLogger(AppEventListener::class.java)
    }

    @Inject
    lateinit var vertx: Vertx
    @Inject
    lateinit var publisher: PoolingProjectionVerticle

    @EventListener
    internal fun onStartupEvent(event: StartupEvent) {
        vertx.registerLocalCodec()
        val deploymentOptions = DeploymentOptions().setHa(false).setInstances(1)
        vertx.deployVerticle(publisher, deploymentOptions)
            .onSuccess { log.info("Successfully started $it") }
            .onFailure { log.error("When starting", it) }
    }

    @EventListener
    internal fun onShutdownEvent(event: ShutdownEvent) {
        vertx.close()
    }

}