package com.example1

import io.github.crabzilla.pgc.PgcEventsScanner
import io.github.crabzilla.pgc.PgcPoolingPublisherVerticle
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import io.vertx.core.Vertx
import io.vertx.pgclient.PgPool
import javax.inject.Named

@Factory
private class AppFactory {

    @Bean
    @Context
    fun eventsPublisherVerticle(vertx: Vertx,
                                appEventsPublisher: AppEventsPublisher,
                                @Named("writeDb") writeDb: PgPool
    ): PgcPoolingPublisherVerticle {
        val eventScanner = PgcEventsScanner(writeDb)
        val verticle = PgcPoolingPublisherVerticle(eventScanner, appEventsPublisher)
        vertx.deployVerticle(verticle)
        return verticle
    }

}
