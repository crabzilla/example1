package com.example1.projections.customers.scylla

import com.datastax.oss.driver.api.core.cql.PreparedStatement
import com.example1.projections.customers.CustomerWriteDao
import io.vertx.cassandra.CassandraClient
import io.vertx.core.Future
import io.vertx.core.Promise
import org.slf4j.LoggerFactory
import javax.annotation.PostConstruct
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named("scylla-style")
class ScyllaCustomerWriteDao(private val cassandra: CassandraClient) : CustomerWriteDao {

    companion object {
        private val log = LoggerFactory.getLogger(ScyllaCustomerWriteDao::class.java)
        private const val UPSERT = "UPDATE customers_summary set name = ?, is_active = ? where id = ?"
        private const val UPDATE_STATUS = "UPDATE customers_summary set is_active = ? where id = ? if EXISTS"
    }

    @PostConstruct
    fun setup() { cassandra
        .execute("CREATE KEYSPACE IF NOT EXISTS example1 WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };")
        .compose { cassandra.execute("USE example1;") }
        .compose { cassandra.execute("CREATE TABLE IF NOT EXISTS customers_summary (id INT, name VARCHAR, is_active BOOLEAN, PRIMARY KEY (id));") }
        .onFailure { log.error("Creating tables", it) }
        .onSuccess { log.info("Tables successfully created") }
    }

    override fun upsert(id: Int, name: String, isActive: Boolean): Future<Void> {
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

    override fun updateStatus(id: Int, isActive: Boolean): Future<Void> {
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
