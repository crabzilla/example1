package com.example1.customers


import io.github.crabzilla.core.BoundedContextName
import io.github.crabzilla.stack.EventRecord
import io.github.crabzilla.stack.EventsPublisher
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.Vertx
import org.slf4j.LoggerFactory
import javax.inject.Singleton

/**
 * Publishes domain events to read model
 */
@Singleton
class CustomerProjectionPublisher(private val vertx: Vertx) : EventsPublisher {

    companion object {
        private val log = LoggerFactory.getLogger(CustomerProjectionPublisher::class.java)
        val boundedContextName = BoundedContextName("example1")
    }

    init {
        log.info("I'm up and will publish events to ${boundedContextName.name}")
    }

    // https://docs.hazelcast.com/imdg/4.2/data-structures/fencedlock.html

    override fun publish(event: EventRecord): Future<Void> {
        val promise = Promise.promise<Void>()
        val asJson = event.toJsonObject()
        vertx.eventBus().request<Void>(CustomerProjectionVerticle.ENDPOINT, asJson) { ar ->
            if (ar.failed()) {
                log.error("When projecting $asJson to ${CustomerProjectionVerticle.ENDPOINT}", ar.cause())
                promise.fail(ar.cause())
            } else {
                if (log.isDebugEnabled) {
                    log.debug("Successfully projected event ${event.eventId} to ${CustomerProjectionVerticle.ENDPOINT}")
                }
                promise.complete()
            }
        }
        return promise.future()
    }

}