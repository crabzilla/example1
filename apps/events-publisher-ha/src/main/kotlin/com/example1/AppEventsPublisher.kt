package com.example1


import io.github.crabzilla.core.BoundedContextName
import io.github.crabzilla.stack.EventRecord
import io.github.crabzilla.stack.EventsPublisher
import io.nats.streaming.StreamingConnection
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.Vertx
import org.slf4j.LoggerFactory

/**
 * Publishes domain events to NATS (single writer process)
 */
class AppEventsPublisher(private val vertx: Vertx, private val nats: StreamingConnection) : EventsPublisher {

    companion object {
        private val log = LoggerFactory.getLogger(AppEventsPublisher::class.java)
        val boundedContextName = BoundedContextName("example1")
    }

    init {
        log.info("I'm up and will publish events to ${boundedContextName.name}")
    }

    override fun publish(event: EventRecord): Future<Long> {
        val promise = Promise.promise<Long>()
        vertx.executeBlocking<Long>({ promise2 ->
            if (log.isDebugEnabled) log.debug("Will publish $event to ${boundedContextName.name}")
            nats.publish(boundedContextName.name, event.toJsonObject().toBuffer().bytes)
            if (log.isDebugEnabled) log.debug("Published $event to ${boundedContextName.name}")
            promise2.complete(event.eventId)
        }, { ar ->
            if (ar.failed()) {
                promise.fail(ar.cause())
                log.error("When publishing event", ar.cause())
            } else {
                promise.complete(ar.result())
            }
        })
        return promise.future()
    }

}