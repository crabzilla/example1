package com.example1

import io.micronaut.context.event.ShutdownEvent
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.vertx.core.Vertx
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppEventListener {

    @Inject
    lateinit var vertx: Vertx

    @EventListener
    internal fun onStartupEvent(event: StartupEvent) {

    }

    @EventListener
    internal fun onShutdownEvent(event: ShutdownEvent) {
        vertx.close()
    }

}