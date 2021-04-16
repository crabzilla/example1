package com.example1.customers

import com.example1.infra.handle
import com.example1.jooq.tables.CustomerSummary.CUSTOMER_SUMMARY
import com.example1.jooq.tables.pojos.CustomerSummary
import io.vertx.core.Future
import io.vertx.core.Promise
import io.zero88.jooqx.DSLAdapter
import io.zero88.jooqx.ReactiveJooqx
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named("postgress")
class PostgressCustomersQueryDao(private val jooqx: ReactiveJooqx) : CustomersQueryDao {

    override fun all(): Future<List<CustomerSummary>> {
        val promise = Promise.promise<List<CustomerSummary>>()
        val sql = jooqx.dsl().select().from(CUSTOMER_SUMMARY).orderBy(CUSTOMER_SUMMARY.ID)
        val dslAdapter = DSLAdapter.fetchMany(CUSTOMER_SUMMARY, CustomerSummary::class.java)
        jooqx.execute(sql, dslAdapter) { it.handle(promise) }
        return promise.future()
    }

}

