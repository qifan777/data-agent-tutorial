package io.github.qifan777.server.agent

object DataAgentSpec {
    const val GRAPH_NAME = "data-agent-main-graph"

    object Retrieval {
        object DocumentMetadataKey {
            const val TABLE_ID = "tableId"
            const val COLUMN_ID = "columnId"
            const val KNOWLEDGE_ID = "knowledgeId"
            const val VECTOR_TYPE = "vectorType"
            const val DATABASE_ID = "databaseId"
            const val BUSINESS_TERM_ID = "businessTermId"
        }

        object VectorType {
            const val QUESTION_KNOWLEDGE = "questionKnowledge"
            const val GLOSSARY_KNOWLEDGE = "glossaryKnowledge"
            const val COLUMN = "column"
            const val TABLE = "table"
        }
    }

}
