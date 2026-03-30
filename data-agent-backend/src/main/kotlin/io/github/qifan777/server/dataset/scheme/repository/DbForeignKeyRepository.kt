package io.github.qifan777.server.dataset.scheme.repository

import io.github.qifan777.server.dataset.scheme.domain.DbForeignKey
import io.github.qifan777.server.dataset.scheme.domain.by
import io.github.qifan777.server.dataset.scheme.domain.databaseId
import io.github.qifan777.server.dataset.scheme.domain.dbTable
import io.github.qifan777.server.dataset.scheme.domain.sourceColumn
import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import java.util.*

interface DbForeignKeyRepository : KRepository<DbForeignKey, UUID> {
    companion object {
        val FETCHER = newFetcher(DbForeignKey::class).by {
            sourceColumn {
                allScalarFields()
                dbTable {
                    name()
                }
            }
            targetColumn {
                allScalarFields()
                dbTable {
                    name()
                }
            }
        }
    }

    fun findByDatabaseId(databaseId: String): List<DbForeignKey> {
        return sql.createQuery(DbForeignKey::class) {
            where(table.sourceColumn.dbTable.databaseId eq databaseId)
            select(table.fetch(FETCHER))
        }.execute()
    }
}