package com.example1

import com.example1.customers.CustomerProjectionPublisher
import com.example1.nats.NatsProjectionPublisher
import io.github.crabzilla.pgc.PgcEventsScanner
import io.github.crabzilla.stack.PoolingProjectionVerticle
import io.micronaut.context.annotation.Factory
import io.vertx.pgclient.PgPool
import javax.inject.Named
import javax.inject.Singleton

@Factory
private class AppFactory {

    @Singleton
    @Named("customers")
    fun publisherVerticle(@Named("customers") eventsPublisher: CustomerProjectionPublisher,
                          @Named("writeDb") writeDb: PgPool
    ): PoolingProjectionVerticle {
        val eventsScanner = PgcEventsScanner(writeDb, "customers")
        return PoolingProjectionVerticle(eventsScanner, eventsPublisher)
    }

    @Singleton
    @Named("nats")
    fun publisherVerticle(@Named("nats") eventsPublisher: NatsProjectionPublisher,
                          @Named("writeDb") writeDb: PgPool
    ): PoolingProjectionVerticle {
        val eventsScanner = PgcEventsScanner(writeDb, "nats-domain-events")
        return PoolingProjectionVerticle(eventsScanner, eventsPublisher)
    }

}
