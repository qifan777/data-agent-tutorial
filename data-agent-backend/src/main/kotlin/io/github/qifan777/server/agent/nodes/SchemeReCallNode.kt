package io.github.qifan777.server.agent.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import io.github.qifan777.server.agent.DataAgentSpec
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder
import org.springframework.stereotype.Component

@Component
class SchemeReCallNode(private val vectorStore: VectorStore) : NodeAction {
    override fun apply(state: OverAllState): Map<String, Any> {
        val rewriteQuery = state.value(DataAgentSpec.Graph.StateKey.Recall.REWRITE_QUERY, "")
        val databaseId = state.value(DataAgentSpec.Graph.StateKey.Input.DATABASE_ID, "")
        val tableDocuments = retrieveTable(rewriteQuery, databaseId)
        val columnDocuments = retrieveColumn(rewriteQuery, databaseId)
        return mapOf(
            DataAgentSpec.Graph.StateKey.Recall.TABLE_SCHEMA to tableDocuments,
            DataAgentSpec.Graph.StateKey.Recall.COLUMN_SCHEMA to columnDocuments
        )
    }

    fun retrieveTable(question: String, databaseId: String): List<Document> {
        val builder = FilterExpressionBuilder()
        val expression = builder.and(
            builder.eq(
                DataAgentSpec.Retrieval.DocumentMetadataKey.VECTOR_TYPE,
                DataAgentSpec.Retrieval.VectorType.TABLE
            ),
            builder.eq(DataAgentSpec.Retrieval.DocumentMetadataKey.DATABASE_ID, databaseId)
        ).build()
        val request = SearchRequest.builder()
            .query(question)
            .filterExpression(expression)
            .topK(10)
            .build()
        return vectorStore.similaritySearch(request)
    }

    fun retrieveColumn(question: String, databaseId: String): List<Document> {
        val builder = FilterExpressionBuilder()
        val expression = builder.and(
            builder.eq(
                DataAgentSpec.Retrieval.DocumentMetadataKey.VECTOR_TYPE,
                DataAgentSpec.Retrieval.VectorType.COLUMN
            ),
            builder.eq(DataAgentSpec.Retrieval.DocumentMetadataKey.DATABASE_ID, databaseId)
        ).build()

        val request = SearchRequest.builder()
            .query(question)
            .filterExpression(expression)
            .topK(30)
            .build()
        return vectorStore.similaritySearch(request)
    }
}
