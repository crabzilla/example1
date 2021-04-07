package com.example1

import io.github.crabzilla.pgc.PgcEventsScanner
import io.github.crabzilla.pgc.PgcPoolingPublisherVerticle
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import io.vertx.circuitbreaker.CircuitBreaker
import io.vertx.circuitbreaker.CircuitBreakerOptions
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
    ): PgcPoolingPublisherVerticle {
        val eventScanner = PgcEventsScanner(writeDb)
        val verticle = PgcPoolingPublisherVerticle(eventScanner, appEventsPublisher, cb(vertx))
        vertx.deployVerticle(verticle)
        return verticle
    }

    fun cb(vertx: Vertx): CircuitBreaker {
        return CircuitBreaker.create(
            "events-publisher-circuit-breaker", vertx,
            CircuitBreakerOptions()
                .setMaxFailures(5) // number of failure before opening the circuit
                .setTimeout(2000) // consider a failure if the operation does not succeed in time
                .setFallbackOnFailure(false) // do we call the fallback on failure
                .setResetTimeout(10000) // time spent in open state before attempting to re-try
            // TODO jitter
        ).openHandler {
            log.warn("Circuit opened")
        }.closeHandler {
            log.warn("Circuit closed")
        }.retryPolicy { retryCount -> retryCount * 1000L }
    }
}
