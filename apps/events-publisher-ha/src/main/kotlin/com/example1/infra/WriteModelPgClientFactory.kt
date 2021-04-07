package com.example1.infra

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions

class WriteModelPgClientFactory {

    fun writeDb(vertx: Vertx, config: JsonObject): PgPool {
        val options = PgConnectOptions()
                .setPort(config.getInteger("DB_PORT"))
                .setHost(config.getString("DB_HOST"))
                .setDatabase(config.getString("DB_NAME"))
                .setUser(config.getString("DB_USER"))
                .setPassword(config.getString("DB_PASSWORD"))
        val pgPoolOptions = PoolOptions().setMaxSize(config.getInteger("DB_POOL_SIZE"))
        return PgPool.pool(vertx, options, pgPoolOptions)
    }

}
