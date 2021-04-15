package com.example1.projections.customers.deprecated

import com.example1.projections.customers.CustomerProjectorVerticle
import io.nats.streaming.Message
import io.nats.streaming.MessageHandler
import io.nats.streaming.StreamingConnection
import io.nats.streaming.Subscription
import io.nats.streaming.SubscriptionOptions
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Named
import javax.inject.Singleton

@Named("nats-projector")
@Singleton
@Deprecated("Use CustomerProjectionVerticle instead")
class NatsCustomerProjectionVerticle(private val streamingConnection: StreamingConnection)
    : AbstractVerticle() {

    companion object {
        private val log = LoggerFactory.getLogger(NatsCustomerProjectionVerticle::class.java)
        private const val topic = "example1"
        private const val durableName = "customers-projection"
        private const val maxInFlight = 10
    }

    private val lastSequence = AtomicLong(0) // TODO persistir em banco ou cache (Coherence?)

    override fun start() {
        vertx.executeBlocking<Void> {
            subscribe()
        }
        log.info("Started consuming events from topic [$topic] " +
                 "and publishing to [${CustomerProjectorVerticle.ENDPOINT}]")
    }

    private fun subscribe() {

        val messageHandler = MessageHandler { msg: Message ->
            if (log.isDebugEnabled) log.debug("I received $msg")
            val asJson = JsonObject(String(msg.data))
            val aggregateName = asJson.getString("aggregateName")
            vertx.eventBus().request<Void>(CustomerProjectorVerticle.ENDPOINT, asJson) { ar ->
                if (ar.failed()) {
                    log.error("When projecting $asJson to $aggregateName", ar.cause())
                } else {
                    if (log.isDebugEnabled) log.debug("Successfully projected event to $aggregateName")
                    msg.ack()
                    lastSequence.set(msg.sequence)
                }
            }
        }

        val subscriptionOptions = SubscriptionOptions
            .Builder()
            .durableName(durableName)
            .maxInFlight(maxInFlight)
            .manualAcks()
            .startAtSequence(lastSequence.get() +1)
            .dispatcher(durableName)
            .build()

        val subscription: Subscription = streamingConnection.subscribe(topic, messageHandler, subscriptionOptions)
    }

}