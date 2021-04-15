package com.example1.projections.customers

import com.example1.jooq.tables.pojos.CustomerSummary
import io.micronaut.context.annotation.Context
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.reactivex.Single
import org.slf4j.LoggerFactory

@Controller("/customers")
@Context
class CustomersQueryController(private val dao: CustomersQueryDao) {

    companion object {
        private val log = LoggerFactory.getLogger(CustomersQueryController::class.java)
    }

    @Get("/")
    fun index(): Single<List<CustomerSummary>> {
        return Single.create { emitter ->

            dao.all()
                .onFailure {
                    log.error("Error", it)
                    emitter.onError(it)
                }
                .onSuccess { list: List<CustomerSummary> ->
                    log.info("Result: $list")
                    emitter.onSuccess(list)
                }


        }
    }

}
