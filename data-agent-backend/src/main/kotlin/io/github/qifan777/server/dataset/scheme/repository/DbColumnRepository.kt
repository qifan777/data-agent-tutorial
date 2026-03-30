package io.github.qifan777.server.dataset.scheme.repository

import io.github.qifan777.server.dataset.scheme.domain.DbColumn
import io.github.qifan777.server.dataset.scheme.domain.by
import org.babyfish.jimmer.spring.repository.KRepository
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
}
