package com.example1.customers

import io.vertx.core.Future
import java.util.UUID

interface CustomerWriteDao {
    fun upsert(id: UUID, name: String, isActive: Boolean): Future<Void>
    fun updateStatus(id: UUID, isActive: Boolean): Future<Void>
}