package io.github.qifan777.server.dataset.scheme.repository

import io.github.qifan777.server.dataset.scheme.domain.DbTable
import io.github.qifan777.server.dataset.scheme.domain.by
import io.github.qifan777.server.dataset.scheme.domain.databaseId
import io.github.qifan777.server.dataset.scheme.domain.name
import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import java.util.*

interface DbTableRepository : KRepository<DbTable, UUID> {
    companion object {
        val FETCHER = newFetcher(DbTable::class).by {
            allScalarFields()
            columns {
                allScalarFields()
            }
        }
    }

    fun findByDatabaseId(databaseId: String): List<DbTable> {
        return sql.createQuery(DbTable::class) {
            where(table.databaseId eq databaseId)
            select(table)
        }.execute()
    }

    fun findByDatabaseIdAndNames(databaseId: String, names: List<String>): List<DbTable> {
        return sql.createQuery(DbTable::class) {
            where(table.databaseId eq databaseId, table.name.valueIn(names))
            select(table.fetch(FETCHER))
        }.execute()
    }
}