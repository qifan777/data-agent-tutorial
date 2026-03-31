package io.github.qifan777.server.agent.datasource

import org.sqlite.SQLiteDataSource
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

object SqliteSchemaDataSourceProvider : SchemaDataSourceProvider {
    private val dataSourceCache = ConcurrentHashMap<String, DataSource>()

    override fun get(databaseId: String): DataSource {
        return dataSourceCache.computeIfAbsent(databaseId) { id ->
            SQLiteDataSource().apply {
                url = "jdbc:sqlite::resource:dev_20240627/dev_databases/$id/$id.sqlite"
            }
        }
    }
}
