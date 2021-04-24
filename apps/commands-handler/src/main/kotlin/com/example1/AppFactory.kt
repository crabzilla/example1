package com.example1

import com.example1.core.customer.Customer
import com.example1.core.customer.CustomerCommand
import com.example1.core.customer.CustomerEvent
import com.example1.core.customer.customerConfig
import io.github.crabzilla.core.BoundedContextName
import io.github.crabzilla.stack.CommandController
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
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

}
