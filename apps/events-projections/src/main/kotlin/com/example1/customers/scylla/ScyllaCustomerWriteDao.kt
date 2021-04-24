package com.example1.customers.scylla

import com.datastax.oss.driver.api.core.cql.PreparedStatement
import com.example1.customers.CustomerWriteDao
import io.vertx.cassandra.CassandraClient
import io.vertx.core.Future
import io.vertx.core.Promise
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named("scylla-style")
class ScyllaCustomerWriteDao(private val cassandra: CassandraClient) : CustomerWriteDao {

    companion object {
        private val log = LoggerFactory.getLogger(ScyllaCustomerWriteDao::class.java)
        private const val UPSERT = "UPDATE example1.customers_summary set name = ?, is_active = ? where id = ?"
        private const val UPDATE_STATUS = "example1.customers_summary set is_active = ? where id = ? if EXISTS"
    }

    override fun upsert(id: UUID, name: String, isActive: Boolean): Future<Void> {
        val promise = Promise.promise<Void>()
        cassandra.prepare(UPSERT)
            .compose { ps: PreparedStatement -> cassandra.execute(ps.bind(name, isActive, id)) }
            .onFailure {
                promise.fail(it)
            }
            .onSuccess {
                promise.complete()
            }
        return promise.future()
    }

    override fun updateStatus(id: UUID, isActive: Boolean): Future<Void> {
        val promise = Promise.promise<Void>()
        cassandra.prepare(UPDATE_STATUS)
            .compose { ps: PreparedStatement -> cassandra.execute(ps.bind(isActive, id)) }
            .onFailure {
                promise.fail(it)
            }
            .onSuccess {
                promise.complete()
            }
        return promise.future()
    }
}
