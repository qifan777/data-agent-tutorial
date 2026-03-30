package io.github.qifan777.server.agent

import io.github.qifan777.server.dataset.knowledge.domain.GlossaryKnowledge
import io.github.qifan777.server.dataset.knowledge.domain.QuestionKnowledge
import io.github.qifan777.server.dataset.scheme.domain.DbColumn
import io.github.qifan777.server.dataset.scheme.domain.DbTable
import org.springframework.ai.document.Document
import java.util.*

fun Map<String, Any>.uuidOrNull(key: String): UUID? {
    return when (val value = this[key]) {
        is UUID -> value
        is String -> value.takeIf { it.isNotBlank() }?.let {
            runCatching { UUID.fromString(it) }.getOrNull()
        }

        else -> null
    }
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

fun DbTable.toDocument(): Document {
    return Document(
        description,
        mapOf(
            DataAgentSpec.Retrieval.DocumentMetadataKey.VECTOR_TYPE to DataAgentSpec.Retrieval.VectorType.TABLE,
            DataAgentSpec.Retrieval.DocumentMetadataKey.DATABASE_ID to databaseId,
            DataAgentSpec.Retrieval.DocumentMetadataKey.TABLE_ID to id
        )
    )
}

fun DbColumn.toDocument(): Document {
    // 确保必要字段已加载，或者在 Repository 层强制 Fetch
    return Document(
        description,
        mapOf(
            DataAgentSpec.Retrieval.DocumentMetadataKey.VECTOR_TYPE to DataAgentSpec.Retrieval.VectorType.COLUMN,
            DataAgentSpec.Retrieval.DocumentMetadataKey.DATABASE_ID to dbTable.databaseId,
            DataAgentSpec.Retrieval.DocumentMetadataKey.TABLE_ID to dbTable.id,
            DataAgentSpec.Retrieval.DocumentMetadataKey.COLUMN_ID to id
        )
    )
}

