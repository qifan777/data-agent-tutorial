package io.github.qifan777.server.shared.datasource

import javax.sql.DataSource

fun interface SchemaDataSourceProvider {
    fun get(databaseId: String): DataSource
}
