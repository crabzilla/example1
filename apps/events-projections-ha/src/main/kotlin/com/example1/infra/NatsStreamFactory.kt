package com.example1.infra

import io.nats.streaming.NatsStreaming
import io.nats.streaming.Options
import io.nats.streaming.StreamingConnection
import io.vertx.core.json.JsonObject
import java.util.concurrent.atomic.AtomicReference

class NatsStreamFactory {

    // https://github.com/nats-io/stan.java#subscriber-rate-limiting
    // https://github.com/nats-io/stan.java#sharing-a-nats-connection
    private val firstConnection = AtomicReference<StreamingConnection>()

    fun createNatConnection(config: JsonObject): StreamingConnection {
        val clusterId = config.getString("NATS_CLUSTER_ID")
        val clientId = config.getString("NATS_CLIENT_ID")
        val clientUrl = config.getString("NATS_URL")
        return if (firstConnection.get() == null) {
            val options = Options.Builder()
                .clusterId(clusterId)
                .clientId(clientId)
                .natsUrl(clientUrl)
                .build()
            firstConnection.set(NatsStreaming.connect(clusterId, clientId, options))
            firstConnection.get()
        } else {
            val options = Options.Builder()
                .natsConn(firstConnection.get().natsConnection)
                .clusterId(clusterId)
                .clientId(clientId)
                .build()
            NatsStreaming.connect(clusterId, clientId, options)
        }
    }

}