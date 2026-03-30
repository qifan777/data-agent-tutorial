package io.github.qifan777.server.dataset.knowledge.domain

import io.github.qifan777.server.agent.DataAgentSpec
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import org.springframework.ai.document.Document
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

fun QuestionKnowledge.toDocument(): Document {

    return Document(
        question,
        mapOf(
            DataAgentSpec.Retrieval.DocumentMetadataKey.VECTOR_TYPE to DataAgentSpec.Retrieval.VectorType.QUESTION_KNOWLEDGE,
            DataAgentSpec.Retrieval.DocumentMetadataKey.KNOWLEDGE_ID to id,
            DataAgentSpec.Retrieval.DocumentMetadataKey.DATABASE_ID to databaseId
        )
    )
}
