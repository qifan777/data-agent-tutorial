package io.github.qifan777.server.dataset.scheme.repository

import io.github.qifan777.server.dataset.scheme.domain.DbColumn
import io.github.qifan777.server.dataset.scheme.domain.by
import io.github.qifan777.server.dataset.scheme.domain.databaseId
import io.github.qifan777.server.dataset.scheme.domain.dbTable
import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import java.util.*

interface DbColumnRepository : KRepository<DbColumn, UUID> {
    companion object {
        val FETCHER = newFetcher(DbColumn::class).by {
            allScalarFields()
            dbTable {
                allScalarFields()
            }
        }
    }

    fun findByDatabaseId(databaseId: String): List<DbColumn> {
        return sql.createQuery(DbColumn::class) {
            where(table.dbTable.databaseId.eq(databaseId))
            select(table.fetch(FETCHER))
        }.execute()
    }
}
