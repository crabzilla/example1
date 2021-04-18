package com.example1

import io.github.crabzilla.core.BoundedContextName
import io.github.crabzilla.example1.Customer
import io.github.crabzilla.example1.CustomerCommand
import io.github.crabzilla.example1.CustomerEvent
import io.github.crabzilla.example1.customerConfig
import io.github.crabzilla.stack.CommandController
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.vertx.cassandra.CassandraClient
import io.vertx.pgclient.PgPool
import javax.inject.Named
import javax.inject.Singleton

@Factory
private class AppFactory {

    val boundedContextName = BoundedContextName("example1")

    @Bean
    @Singleton
    @Named("postgress")
    fun pgCustomerCommandController(@Named("writeDb") writeDb: PgPool):
            CommandController<Customer, CustomerCommand, CustomerEvent> {
        return io.github.crabzilla.pgc.CommandControllerFactory
            .createPublishingTo(boundedContextName.name, customerConfig, writeDb)
    }

    @Bean
    @Singleton
    @Named("cassandra")
    fun cassandraCustomerCommandController(cassandra: CassandraClient):
            CommandController<Customer, CustomerCommand, CustomerEvent> {
        return io.github.crabzilla.cassandra.CommandControllerFactory
            .createPublishingTo(boundedContextName.name, customerConfig, cassandra)
    }

}
