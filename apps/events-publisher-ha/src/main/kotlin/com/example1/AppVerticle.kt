package com.example1

import com.example1.infra.NatsStreamFactory
import com.example1.infra.WriteModelPgClientFactory
import io.github.crabzilla.pgc.PgcEventsScanner
import io.github.crabzilla.pgc.PgcPoolingPublisherVerticle
import io.nats.streaming.StreamingConnection
import io.vertx.circuitbreaker.CircuitBreaker
import io.vertx.circuitbreaker.CircuitBreakerOptions
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.JsonObject
import io.vertx.pgclient.PgPool
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory

class AppVerticle: AbstractVerticle() {

    companion object {
        val log = LoggerFactory.getLogger(AppVerticle::class.java)
        val node = ManagementFactory.getRuntimeMXBean().name
        val singletonEndpoint = "com.example1.events-publisher-ha"
    }

    lateinit var streamingConnection: StreamingConnection
    lateinit var writeDb: PgPool

    override fun start() {
        vertx.eventBus()
            .consumer<String>(singletonEndpoint) { msg ->
                msg.reply("Hi $ ${msg.body()}. Please notice I'm already working from node $node")
            }
        val config = this.config()
        try {
            writeDb = WriteModelPgClientFactory().writeDb(com.example1.vertx, config)
            val eventScanner = PgcEventsScanner(writeDb)
            streamingConnection = NatsStreamFactory().createNatConnection(config)
            val appEventsPublisher = AppEventsPublisher(com.example1.vertx, streamingConnection)
            val verticle = PgcPoolingPublisherVerticle(eventScanner, appEventsPublisher, cb())
            val deploymentOptions = DeploymentOptions().setHa(true).setInstances(1).setConfig(config)
            vertx.deployVerticle(verticle, deploymentOptions)
                .onFailure { log.error("When starting ", it) }
                .onSuccess { log.info("*** deployed") }
        } catch (e: Exception) {
            log.error("When starting ", e)
        }
    }

    fun cb(): CircuitBreaker {
        return CircuitBreaker.create(
            "pgc-pooling-circuit-breaker", vertx,
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

    override fun stop() {
        try {
            log.info("Closing resources")
            streamingConnection.close()
            super.stop()
            log.info("Stopped")
        } catch (e: Exception) {
            log.error("When closing resources", e)
        }
    }

}