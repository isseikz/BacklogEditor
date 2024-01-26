package com.isseikz.backlogeditor.source

import kotlinx.coroutines.flow.StateFlow

interface DataSource<T> {
    val name: String
    val dataFlow: StateFlow<List<T>>
    suspend fun create(data: T): Result<Unit>
    suspend fun read(): Result<List<T>>
    suspend fun update(data: T): Result<T>
    suspend fun delete(data: T): Result<T>
}
