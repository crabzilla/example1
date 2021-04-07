package com.example1


import io.github.crabzilla.core.BoundedContextName
import io.github.crabzilla.stack.EventRecord
import io.github.crabzilla.stack.EventsPublisher
import io.micronaut.context.annotation.Context
import io.nats.streaming.StreamingConnection
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.Vertx
import org.slf4j.LoggerFactory
import javax.inject.Singleton

/**
 * Publishes domain events to NATS (single writer process)
 */
//@Singleton
class AppEventsPublisher(private val vertx: Vertx, private val nats: StreamingConnection) : EventsPublisher {

    companion object {
        private val log = LoggerFactory.getLogger(AppEventsPublisher::class.java)
        val boundedContextName = BoundedContextName("example1")
    }

    init {
        log.info("I'm up and will publish events to ${boundedContextName.name}")
    }

    override fun publish(eventRecords: List<EventRecord>): Future<Long> {
        val promise = Promise.promise<Long>()
        vertx.executeBlocking<Long>( { promise2 ->
            var lastPublished: Long? = null
            var error = false
            for (event in eventRecords) {
                try {
                    log.info("Will publish $event to ${boundedContextName.name}")
                    nats.publish(boundedContextName.name, event.toJsonObject().toBuffer().bytes)
                    if (log.isDebugEnabled) log.debug("Published $event to ${boundedContextName.name}")
                    lastPublished = event.eventId
                } catch (e: Exception) {
                    log.error("When publishing $event", e)
                    if (lastPublished == null) {
                        promise2.fail(e)
                    } else {
                        promise2.complete(lastPublished)
                    }
                    error = true
                    break
                }
            }
            if (!error) {
                promise2.complete(lastPublished)
            }
        }, { ar ->
            if (ar.failed()) {
                promise.fail(ar.cause())
            } else {
                promise.complete(ar.result())
            }
        })
        return promise.future()
    }

}