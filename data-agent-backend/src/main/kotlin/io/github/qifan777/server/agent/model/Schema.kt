package io.github.qifan777.server.agent.model

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.qifan777.server.shared.datasource.SchemaDataSourceProvider
import io.github.qifan777.server.shared.datasource.SqliteSchemaDataSourceProvider
import io.github.qifan777.server.dataset.scheme.domain.dto.DbForeignKeySchemaView
import io.github.qifan777.server.dataset.scheme.domain.dto.DbTableSchemaView
import io.github.qifan777.server.dataset.scheme.domain.toExpression
import java.sql.Connection
import java.sql.SQLException

private val logger = KotlinLogging.logger {}

data class Schema(
    val databaseId: String,
    val dbTables: List<DbTableSchemaView>,
    val dbForeignKeys: List<DbForeignKeySchemaView>,
    val enableExampleSampling: Boolean = false
) {
    fun buildSchemePrompt(
        dataSourceProvider: SchemaDataSourceProvider = SqliteSchemaDataSourceProvider,
    ): String {
        val schemeBuilder = StringBuilder()
        schemeBuilder.append("【DB_ID】 $databaseId\n")
        dbTables.forEach {
            schemeBuilder.append(buildTablePrompt(it, dataSourceProvider))
        }
        val keys = dbForeignKeys.joinToString("\n") { it.toExpression() }
        schemeBuilder.append("【Foreign keys】\n$keys")
        logger.info { "scheme prompt $schemeBuilder" }
        return schemeBuilder.toString()
    }

    fun buildTablePrompt(dbTable: DbTableSchemaView, dataSourceProvider: SchemaDataSourceProvider): String {
        val builder = StringBuilder()
        val primaryKeys = dbTable.columns.filter { it.isPrimaryKey }.map { it.name }.toSet()
        val examplesByColumn = if (enableExampleSampling) loadExamples(dbTable, dataSourceProvider) else emptyMap()
        builder.append("# Table: ${dbTable.name}\n[\n")
        dbTable.columns.forEach { columnDto ->
            builder.append("(${columnDto.name}: ${columnDto.type}\n, ${columnDto.description}, ")
            if (primaryKeys.contains(columnDto.name)) {
                builder.append("primaryKey, ")
            }
            val examples = examplesByColumn[columnDto.name].orEmpty()
            builder.append("Examples: [$examples]),\n")
        }
        builder.append("]\n")
        return builder.toString()
    }

    private fun loadExamples(
        dbTable: DbTableSchemaView,
        dataSourceProvider: SchemaDataSourceProvider
    ): Map<String, List<String>> {
        return runCatching {
            dataSourceProvider.get(databaseId).connection.use { connection ->
                dbTable.columns.associate { column ->
                    column.name to fetchDistinctValues(connection, dbTable.name, column.name, 3)
                }
            }
        }.onFailure { ex ->
            logger.warn(ex) {
                "load schema examples failed for table=${dbTable.name}, databaseId=$databaseId, fallback to empty examples"
            }
        }.getOrElse { emptyMap() }
    }

    fun fetchDistinctValues(
        connection: Connection,
        fullTableName: String?,
        columnName: String?,
        limit: Int
    ): List<String> {
        val values: MutableList<String> = mutableListOf()
        val sql = String.format(
            "SELECT DISTINCT `%s` FROM %s WHERE `%s` IS NOT NULL LIMIT %d",
            columnName, fullTableName, columnName, limit
        )
        try {
            connection.createStatement().use { stmt ->
                stmt.executeQuery(sql).use { rs ->
                    while (rs.next()) {
                        val value: String? = rs.getString(1)
                        if (!value.isNullOrBlank()) {
                            values.add(value)
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            // 某些列类型可能不支持 DISTINCT，这里忽略
        }
        return values
    }
}
