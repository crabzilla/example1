package com.example1

import io.github.crabzilla.pgc.PgcEventsScanner
import io.github.crabzilla.pgc.PgcPoolingProjectionVerticle
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import io.vertx.core.Vertx
import io.vertx.pgclient.PgPool
import org.slf4j.LoggerFactory
import javax.inject.Named

@Factory
private class AppFactory {

    companion object {
        private val log = LoggerFactory.getLogger(AppFactory::class.java)
    }

    @Bean
    @Context
    fun eventsPublisherVerticle(vertx: Vertx,
                                appEventsPublisher: AppEventsPublisher,
                                @Named("writeDb") writeDb: PgPool
    ): PgcPoolingProjectionVerticle {
        val eventsScanner = PgcEventsScanner(writeDb, "nats-domain-events")
        return PgcPoolingProjectionVerticle(eventsScanner, appEventsPublisher)
    }
}
