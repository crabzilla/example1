package com.example1

import com.example1.infra.registerLocalCodec
import io.micronaut.context.event.ShutdownEvent
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.vertx.core.Vertx
import io.vertx.pgclient.PgPool
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AppEventListener {

    companion object {
        private val log = LoggerFactory.getLogger(AppEventListener::class.java)
    }

    @Inject
    lateinit var vertx: Vertx
    @Named("writeDb")
    lateinit var writeDb: PgPool
//    @Inject
//    lateinit var cassandra : CassandraClient

    @EventListener
    internal fun onStartupEvent(event: StartupEvent) {
        vertx.registerLocalCodec()
        // createCassandraTables()
    }

    @EventListener
    internal fun onShutdownEvent(event: ShutdownEvent) {
        vertx.close()
        writeDb.close()
        // cassandra.close()
    }

//    fun createCassandraTables() {
//        vertx.executeBlocking<Void> { promise ->
//            cassandra
//                .execute(
//                    "CREATE KEYSPACE IF NOT EXISTS example1 " +
//                            "WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };"
//                )
//                .compose { cassandra.execute("USE example1;") }
//                .compose { cassandra.execute("CREATE TABLE IF NOT EXISTS example1.snapshots (ar_id INT, ar_name VARCHAR, version INT, json_content VARCHAR, PRIMARY KEY (ar_id, ar_name));") }
//                .compose { cassandra.execute("CREATE TABLE IF NOT EXISTS example1.events (event_id timeuuid, event_payload VARCHAR, ar_name VARCHAR, ar_id INT, version INT, cmd_id VARCHAR, PRIMARY KEY (event_id, ar_id, ar_name));") }
//                .onSuccess { promise.complete() }
//                .onFailure { promise.fail(it) }
//        }.onFailure {
//            log.error("Failure", it)
//        }
//            .onSuccess {
//                log.info("Success!")
//            }
//    }

}