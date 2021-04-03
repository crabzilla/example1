package com.example1.infra

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import io.vertx.core.Vertx
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import javax.inject.Named

@Factory
class ReadModelPgClientFactory {

    @Context
    @Named("readDb")
    fun readDb(vertx: Vertx, config: ReadDbConfig): PgPool {
        val options = PgConnectOptions()
                .setPort(config.port!!)
                .setHost(config.host)
                .setDatabase(config.dbName)
                .setUser(config.dbUser)
                .setPassword(config.dbPassword)
        val pgPoolOptions = PoolOptions().setMaxSize(config.poolSize)
        return PgPool.pool(vertx, options, pgPoolOptions)
    }

    @ConfigurationProperties("read.database")
    class ReadDbConfig  {
        var host: String? = "0.0.0.0"
        var port: Int? = 5432
        var dbName: String? = "example1_read"
        var dbUser: String? = "user1"
        var dbPassword: String? = "pwd1"
        var poolSize: Int = 6
    }
}
