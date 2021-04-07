package com.example1

import com.example1.infra.VertxFactory
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory


lateinit var vertx: Vertx
lateinit var eventBus: EventBus

suspend fun main(args: Array<String>) {

	val log = LoggerFactory.getLogger("Application")

	log.info( "Starting vertx with ${args.size}")

	args.forEach { arg -> log.info(arg) }

	Runtime.getRuntime().addShutdownHook(object : Thread() {
		override fun run() {
			vertx.close()
		}
	})

	vertx = VertxFactory().clusteredVertx()
	eventBus = vertx.eventBus()

	if (args.isNotEmpty() && "standby=true" == args[0]) {
		log.info( "Started in standby mode ")
		return
	}

	val node = ManagementFactory.getRuntimeMXBean().name

	vertx.eventBus().request<String>(AppVerticle.singletonEndpoint, node) { resp ->
		if (resp.failed()) {
			val config = config()
			val deploymentOptions = DeploymentOptions().setHa(true).setInstances(1).setConfig(config)
				vertx.deployVerticle(AppVerticle::class.java.name, deploymentOptions)
					.onSuccess { log.info( "Started ") }
					.onFailure { log.error("When starting ", it) }
		} else {
			log.info("Started as standby since node ${resp.result()} is the current owner of this verticle")
		}
	}

}

fun config() : JsonObject {
	val json = JsonObject()
	json.put("prefix", "com.example1")
	// write database
	json.put("DB_HOST", "0.0.0.0")
	json.put("DB_PORT", 5432)
	json.put("DB_NAME", "example1_write")
	json.put("DB_USER", "user1")
	json.put("DB_PASSWORD", "pwd1")
	json.put("DB_POOL_SIZE", 6)
	// NATS
	json.put("NATS_CLUSTER_ID", "test-cluster")
	json.put("NATS_CLIENT_ID", "events-publisher-ha")
	json.put("NATS_URL", "nats://localhost:4222")
	return json
}

