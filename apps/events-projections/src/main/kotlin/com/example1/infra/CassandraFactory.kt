package com.example1.infra

import com.datastax.oss.driver.api.core.CqlSessionBuilder
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import io.vertx.cassandra.CassandraClient

import io.vertx.cassandra.CassandraClientOptions
import io.vertx.core.Vertx
import javax.inject.Singleton

@Factory
class CassandraFactory {

    @Bean
    @Context
    fun shared(vertx: Vertx): CassandraClient {
        val options = CassandraClientOptions(CqlSessionBuilder().withLocalDatacenter("datacenter1"))
            .addContactPoint("localhost", 9042)
//            .setKeyspace("example1")
        return CassandraClient.createShared(vertx, "sharedClientName", options)
    }
}