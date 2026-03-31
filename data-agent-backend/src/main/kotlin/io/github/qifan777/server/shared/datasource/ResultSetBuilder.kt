package io.github.qifan777.server.shared.datasource

import io.github.qifan777.server.agent.model.SqlResultSet
import java.sql.ResultSet
import java.sql.SQLException

object ResultSetBuilder {

    @Throws(SQLException::class)
    fun buildFrom(rs: ResultSet): SqlResultSet {
        val metaData = rs.metaData
        val columnsCount = metaData.columnCount

        // 获取原始列名
        val rowHead = (1..columnsCount).map { i ->
            metaData.getColumnLabel(i)
        }

        val rawData = mutableListOf<Map<String, String>>()
        var count = 0

        while (rs.next() && count < 1000) {
            val kv = rowHead.associateWith { h ->
                rs.getString(h) ?: ""
            }
            rawData.add(kv)
            count++
        }

        // 清理列名和数据
        val cleanedHead = cleanColumnNames(rowHead)
        val cleanedData = cleanResultSet(rawData)

        return SqlResultSet(
            column = cleanedHead,
            data = cleanedData
        )
    }

    private fun cleanColumnNames(columnNames: List<String>): List<String> {
        return columnNames.map { it.replace("`", "").replace("\"", "") }
    }

    private fun cleanResultSet(data: List<Map<String, String>>): List<Map<String, String>> {
        return data.map { row ->
            row.mapKeys { (key, _) ->
                key.replace("`", "").replace("\"", "")
            }
        }
    }
}
