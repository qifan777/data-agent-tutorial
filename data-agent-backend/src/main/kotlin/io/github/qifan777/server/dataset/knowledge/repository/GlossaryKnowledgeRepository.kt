package io.github.qifan777.server.dataset.knowledge.repository

import io.github.qifan777.server.dataset.knowledge.domain.GlossaryKnowledge
import io.github.qifan777.server.dataset.knowledge.domain.databaseId
import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import java.util.*

interface GlossaryKnowledgeRepository : KRepository<GlossaryKnowledge, UUID> {
    fun findByDatabaseId(databaseId: String): List<GlossaryKnowledge> {
        return sql.createQuery(GlossaryKnowledge::class) {
            where(table.databaseId eq databaseId)
            select(table)
        }.execute()
    }
}