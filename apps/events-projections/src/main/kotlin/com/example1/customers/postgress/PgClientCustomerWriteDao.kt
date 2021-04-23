package com.example1.customers.postgress

import com.example1.infra.handleVoid
import com.example1.customers.CustomerWriteDao
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.Tuple
import java.util.UUID
import javax.inject.Named
import javax.inject.Singleton

/**
 * Read model repository
 */
@Singleton
@Named("pg-client-style")
class PgClientCustomerWriteDao(@Named("readDb") private val pool: PgPool) : CustomerWriteDao {

    override fun upsert(id: UUID, name: String, isActive: Boolean): Future<Void> {
        val promise = Promise.promise<Void>()
        pool.preparedQuery("INSERT INTO customer_summary (id, name, is_active) VALUES ($1, $2, $3) " +
                "ON CONFLICT (id) DO UPDATE SET name = $2, is_active = $3")
            .execute(Tuple.of(id, name, isActive)) { it.handleVoid(promise) }
        return promise.future()
    }

    override fun updateStatus(id: UUID, isActive: Boolean): Future<Void> {
        val promise = Promise.promise<Void>()
        pool.preparedQuery("UPDATE customer_summary set is_active = $2 where id = $1")
            .execute(Tuple.of(id)) { it.handleVoid(promise) }
        return promise.future()
    }
}