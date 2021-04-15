package com.example1.queries.customers

import com.example1.jooq.tables.pojos.CustomerSummary
import io.vertx.core.Future

interface CustomersQueryDao {
    fun all(): Future<List<CustomerSummary>>
}