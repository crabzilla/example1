package com.example1

import com.example1.infra.NatsStreamFactory
import com.example1.infra.WriteModelPgClientFactory
import io.github.crabzilla.pgc.PgcEventsScanner
import io.github.crabzilla.stack.PoolingProjectionVerticle
import io.nats.streaming.StreamingConnection
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Promise
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

    override fun start(promise: Promise<Void>) {
        vertx.eventBus()
            .consumer<String>(singletonEndpoint) { msg ->
                log.info("Node [${msg.body()}] just asked to start this verticle. I will answer that I'm already " +
                        "working from this node [$node]")
                msg.reply(node)
            }
        val config = this.config()
        try {
            writeDb = WriteModelPgClientFactory().writeDb(com.example1.vertx, config)
            streamingConnection = NatsStreamFactory().createNatConnection(config)
            val eventsScanner = PgcEventsScanner(writeDb, "nats-domain-events")
            val eventsPublisher = AppEventsPublisher(com.example1.vertx, streamingConnection)
            val verticle = PoolingProjectionVerticle(eventsScanner, eventsPublisher)
            val deploymentOptions = DeploymentOptions().setHa(true).setInstances(1).setConfig(config)
            vertx.deployVerticle(verticle, deploymentOptions)
                .onFailure {
                    log.error("When deploying ", it)
                    promise.fail(it)
                }
                .onSuccess {
                    log.info("*** deployed")
                    promise.complete()
                }
        } catch (e: Exception) {
            log.error("When starting ", e)
            promise.fail(e)
        }
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