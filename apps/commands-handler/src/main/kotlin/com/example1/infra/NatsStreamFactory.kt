package com.example1.infra

import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Prototype
import io.micronaut.context.annotation.Value
import io.nats.streaming.NatsStreaming
import io.nats.streaming.Options
import io.nats.streaming.StreamingConnection
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Singleton

@Factory
class NatsStreamFactory {

    // https://github.com/nats-io/stan.java#subscriber-rate-limiting
    // https://github.com/nats-io/stan.java#sharing-a-nats-connection
    private val firstConnection = AtomicReference<StreamingConnection>()

    @Context
    fun options(config: NatsStreamingConfig) : Options {
        return Options.Builder()
            .clusterId(config.clusterId)
            .clientId(config.clientId)
            .natsUrl(config.getUrl())
            .build()
    }

    @Prototype
    fun createNatConnection(config: NatsStreamingConfig, options: Options): StreamingConnection {
        return if (firstConnection.get() == null) {
            firstConnection.set(NatsStreaming.connect(config.clusterId, config.clientId, options))
            firstConnection.get()
        } else {
            val streamingOptionsTwo = Options.Builder()
                .natsConn(firstConnection.get().natsConnection)
                .clusterId(config.clusterId)
                .clientId(config.clientId)
                .build()
            NatsStreaming.connect(config.clusterId, config.clientId, streamingOptionsTwo)
        }
    }

    @Singleton
    class NatsStreamingConfig{

        val NATS_PROTOCOL = "nats://"

        @Value("\${nats.host}")
        lateinit var host :String

        @Value("\${nats.port}")
        var port :Int = 4222

        @Value("\${nats.user}")
        lateinit var user :String

        @Value("\${nats.password}")
        lateinit var password :String

        @Value("\${nats.client-id}")
        lateinit var clientId :String

        @Value("\${nats.cluster-id}")
        lateinit var clusterId :String

        fun getUrl(): String? {
            var url: String? = null
            url = if (Objects.nonNull(user) || Objects.nonNull(password)) {
                "$NATS_PROTOCOL$user:$password@$host:$port"
            } else {
                "$NATS_PROTOCOL$host:$port"
            }
            return url
        }
    }

}