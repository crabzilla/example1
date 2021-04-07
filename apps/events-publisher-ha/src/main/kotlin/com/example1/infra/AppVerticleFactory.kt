package com.example1.infra

import com.example1.AppEventsPublisher
import com.example1.vertx
import io.github.crabzilla.pgc.PgcEventsScanner
import io.github.crabzilla.pgc.PgcPoolingPublisherVerticle
import io.vertx.core.Promise
import io.vertx.core.Verticle
import io.vertx.core.json.JsonObject
import io.vertx.core.spi.VerticleFactory
import java.util.concurrent.Callable

//class AppVerticleFactory(val config: JsonObject) : VerticleFactory {
//
//    override fun prefix(): String = config.getString("prefix")
//
//    override fun createVerticle(
//        verticleName: String,
//        classLoader: ClassLoader,
//        promise: Promise<Callable<Verticle>>
//    ) {
//        println(verticleName)
//        try {
//            val writeDb = WriteModelPgClientFactory().writeDb(vertx, config)
//            val eventScanner = PgcEventsScanner(writeDb)
//            val streamingConnection = NatsStreamFactory().createNatConnection(config)
//            val appEventsPublisher = AppEventsPublisher(vertx, streamingConnection)
//            val verticle = PgcPoolingPublisherVerticle(eventScanner, appEventsPublisher)
//            promise.complete { verticle }
//        } catch (e: Exception) {
//            promise.fail(e)
//        }
//    }
//}