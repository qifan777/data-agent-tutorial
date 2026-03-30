package io.github.qifan777.server.dataset.knowledge.domain

import io.github.qifan777.server.agent.DataAgentSpec
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import org.springframework.ai.document.Document
import java.util.UUID


@Entity
interface GlossaryKnowledge  {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID
    val databaseId: String
    val term: String
    val description: String
    val synonyms: String?
}

fun GlossaryKnowledge.toDocument(): Document {
    return Document(
        "业务名词: $term, 说明: $description, 同义词: $synonyms",
        mapOf(
            DataAgentSpec.Retrieval.DocumentMetadataKey.VECTOR_TYPE to DataAgentSpec.Retrieval.VectorType.GLOSSARY_KNOWLEDGE,
            DataAgentSpec.Retrieval.DocumentMetadataKey.DATABASE_ID to databaseId,
            DataAgentSpec.Retrieval.DocumentMetadataKey.BUSINESS_TERM_ID to id
        )
    )
}