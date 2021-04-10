package com.example1.customers

import com.example1.core.customer.CustomerEvent
import com.example1.core.customer.CustomerSerialization.customerJson
import com.example1.customers.dao.CustomerWriteDao
import io.github.crabzilla.core.DOMAIN_EVENT_SERIALIZER
import io.micronaut.context.annotation.Context
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import javax.inject.Named

@Context
class CustomerProjectionVerticle(@Named("jooq-style") private val repo: CustomerWriteDao) : AbstractVerticle() {

    companion object {
        private val log = LoggerFactory.getLogger(CustomerProjectionVerticle::class.java)
        const val ENDPOINT = "customer.projection"
    }

    override fun start() {
        vertx.eventBus()
            .consumer<JsonObject>(ENDPOINT) { msg ->
                val asJson = msg.body()
                val aggregateId = asJson.getInteger("aggregateId")
                // TODO use cache for idempotency val eventId = asJson.getLong("eventId")
                val eventAsJson = asJson.getJsonObject("eventAsjJson")
                project(aggregateId, eventAsJson)
                    .onFailure { msg.fail(500, it.message) }
                    .onSuccess { msg.reply(true)}
            }
        log.info("Started on endpoint [$ENDPOINT]")
    }

    private fun project(id: Int, eventAsJson: JsonObject): Future<Void> {
        val event = customerJson.decodeFromString(DOMAIN_EVENT_SERIALIZER, eventAsJson.toString()) as CustomerEvent
       //  if (log.isDebugEnabled) log.debug("Will project event $event to read model")
        return when (event) {
            is CustomerEvent.CustomerRegistered -> repo.upsert(id, event.name, false)
            is CustomerEvent.CustomerActivated -> repo.updateStatus(id, true)
            is CustomerEvent.CustomerDeactivated -> repo.updateStatus(id, false)
        }
    }

}