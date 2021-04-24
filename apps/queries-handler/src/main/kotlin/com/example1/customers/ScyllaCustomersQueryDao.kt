package com.example1.customers

import com.example1.jooq.tables.pojos.CustomerSummary
import io.vertx.cassandra.CassandraClient
import io.vertx.core.Future
import io.vertx.core.Promise
import org.slf4j.LoggerFactory
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named("scylla")
class ScyllaCustomersQueryDao(private val cassandra: CassandraClient) : CustomersQueryDao {

    companion object {
        private val log = LoggerFactory.getLogger(ScyllaCustomersQueryDao::class.java)
    }

    override fun all(): Future<List<CustomerSummary>> {
        val promise = Promise.promise<List<CustomerSummary>>()
        cassandra.executeWithFullFetch("SELECT * FROM example1.customers_summary") {
                executeWithFullFetch ->
            if (executeWithFullFetch.succeeded()) {
                val rows = executeWithFullFetch.result()
                    .map { row ->
                        CustomerSummary(
                            row.getUuid("id"),
                            row.getString("name"),
                            row.getBoolean("is_active")
                        )
                    }
                promise.complete(rows)
            } else {
                log.error("Unable to execute the query", executeWithFullFetch.cause())
                promise.fail(executeWithFullFetch.cause())
            }
        }
        return promise.future()
    }

}