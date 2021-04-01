package com.example1.projectors.customer

import io.vertx.core.Future

interface CustomerWriteDao {
    fun upsert(id: Int, name: String, isActive: Boolean): Future<Void>
    fun updateStatus(id: Int, isActive: Boolean): Future<Void>
}