package io.github.qifan777.server.agent.datasource

import javax.sql.DataSource

fun interface SchemaDataSourceProvider {
    fun get(databaseId: String): DataSource
}
