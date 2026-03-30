package io.github.qifan777.server.dataset.knowledge.repository

import io.github.qifan777.server.dataset.knowledge.domain.QuestionKnowledge
import io.github.qifan777.server.dataset.knowledge.domain.databaseId
import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import java.util.*

interface QuestionKnowledgeRepository : KRepository<QuestionKnowledge, UUID>{
    fun findByDatabaseId(databaseId: String): List<QuestionKnowledge>{
        return sql.createQuery(QuestionKnowledge::class){
            where(table.databaseId eq databaseId)
            select(table)
        }.execute()
    }

}