package com.example1.customers

import com.example1.core.customer.CustomerEvent
import com.example1.core.customer.CustomerSerialization.customerJson
import io.github.crabzilla.core.DOMAIN_EVENT_SERIALIZER
import io.micronaut.context.annotation.Context
import io.nats.streaming.Message
import io.nats.streaming.MessageHandler
import io.nats.streaming.StreamingConnection
import io.nats.streaming.Subscription
import io.nats.streaming.SubscriptionOptions
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.PostConstruct
import javax.inject.Named

@Context
class CustomerProjectionVerticle(private val streamingConnection: StreamingConnection,
                                 @Named("jooq-style") private val repo: CustomerWriteDao
) : AbstractVerticle() {

    companion object {
        private val log = LoggerFactory.getLogger(CustomerProjectionVerticle::class.java)
        private const val topic = "example1"
        private const val durableName = "customers-projection"
        private const val maxInFlight = 100
    }

    private val lastSequence = AtomicLong(0) // TODO persistir em banco ou cache (Coherence?)

    @PostConstruct
    fun deploy(vertx: Vertx) {
        vertx.deployVerticle(this)
            .compose {
                vertx.executeBlocking<Void> {
                    subscribe()
                }
            }.onSuccess { log.info("Successfully started") }
            .onFailure { log.error("When starting", it) }
    }

    override fun start() {
        log.info("I'm up and subscribing to events from topic $topic")

        vertx.eventBus()
            .consumer<JsonObject>(topic) { msg ->
                val asJson = msg.body()
                val aggregateId = asJson.getInteger("aggregateId")
                val eventId = asJson.getLong("eventId") // TODO use cache for idempotency
                val eventAsJson = asJson.getJsonObject("eventAsjJson")
                project(eventId, aggregateId, eventAsJson)
                    .onFailure { msg.fail(500, it.message) }
                    .onSuccess { msg.reply(true)}
            }

        log.info("Started consuming events from topic [$topic]")
    }

    private fun subscribe() {

        val messageHandler = MessageHandler { msg: Message ->
            log.info("I received $msg")
            val asJson = JsonObject(String(msg.data))
            val aggregateName = asJson.getString("aggregateName")
            vertx.eventBus().request<Void>(topic, asJson) { ar ->
                if (ar.failed()) {
                    log.error("When projecting $asJson to $aggregateName", ar.cause())
                } else {
                    log.info("Successfully projected event to $aggregateName")
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

    private fun project(eventId: Long, id: Int, eventAsJson: JsonObject): Future<Void> {
        val event = customerJson.decodeFromString(DOMAIN_EVENT_SERIALIZER, eventAsJson.toString()) as CustomerEvent
        log.info("Will project event $event to read model")
        return when (event) {
            is CustomerEvent.CustomerRegistered -> repo.upsert(id, event.name, false)
            is CustomerEvent.CustomerActivated -> repo.updateStatus(id, true)
            is CustomerEvent.CustomerDeactivated -> repo.updateStatus(id, false)
        }
    }

}