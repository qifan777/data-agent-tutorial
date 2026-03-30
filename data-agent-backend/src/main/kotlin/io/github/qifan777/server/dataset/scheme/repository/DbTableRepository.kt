package io.github.qifan777.server.dataset.scheme.repository

import io.github.qifan777.server.dataset.scheme.domain.DbTable
import io.github.qifan777.server.dataset.scheme.domain.databaseId
import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import java.util.*

interface DbTableRepository : KRepository<DbTable, UUID> {
    fun findByDatabaseId(databaseId: String): List<DbTable> {
        return sql.createQuery(DbTable::class) {
            where(table.databaseId eq databaseId)
            select(table)
        }.execute()
    }
}