package io.github.qifan777.server.dataset.knowledge.domain

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import java.util.*

@Entity
interface QuestionKnowledge {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID
    val databaseId: String
    val question: String
    val answer: String
}

