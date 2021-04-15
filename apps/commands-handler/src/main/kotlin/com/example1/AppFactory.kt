package com.example1

import io.github.crabzilla.core.BoundedContextName
import io.github.crabzilla.example1.Customer
import io.github.crabzilla.example1.CustomerCommand
import io.github.crabzilla.example1.CustomerEvent
import io.github.crabzilla.example1.customerConfig
import io.github.crabzilla.pgc.CommandControllerFactory
import io.github.crabzilla.stack.CommandController
import io.github.crabzilla.stack.InMemorySnapshotRepo
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import io.vertx.core.Vertx
import io.vertx.pgclient.PgPool
import javax.inject.Named

@Factory
private class AppFactory {

    val boundedContextName = BoundedContextName("example1")

    @Bean
    @Context
    fun customerCommandController(vertx: Vertx, @Named("writeDb") writeDb: PgPool):
            CommandController<Customer, CustomerCommand, CustomerEvent> {
        val snapshotRepo = InMemorySnapshotRepo(vertx.sharedData(), customerConfig)
        return CommandControllerFactory.createPublishingTo(boundedContextName.name, customerConfig, writeDb, snapshotRepo)
    }

}
