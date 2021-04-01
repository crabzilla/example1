package com.example1

import com.example1.projectors.customer.CustomerProjectorVerticle
import com.example1.projectors.customer.CustomerWriteDao
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import io.vertx.core.Vertx
import javax.inject.Named

@Factory
private class AppFactory {

    @Bean
    @Context
    fun eventsPublisherVerticle(vertx: Vertx, @Named("jooq-style") repo: CustomerWriteDao
    ): CustomerProjectorVerticle {
        val verticle = CustomerProjectorVerticle(repo)
        vertx.deployVerticle(verticle)
        return verticle
    }

}
