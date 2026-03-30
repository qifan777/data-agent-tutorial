package io.github.qifan777.server.agent.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.qifan777.server.agent.DataAgentSpec
import io.github.qifan777.server.agent.model.EvidenceQueryRewriteDTO
import io.github.qifan777.server.agent.prompt.PromptManager
import io.github.qifan777.server.agent.uuidOrNull
import io.github.qifan777.server.dataset.knowledge.repository.QuestionKnowledgeRepository
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.ai.document.Document
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class EvidenceRecallNode(
    private val chatModel: ChatModel,
    private val vectorStore: VectorStore,
    private val questionKnowledgeRepository: QuestionKnowledgeRepository,
    private val promptManager: PromptManager
) : NodeAction {
    override fun apply(state: OverAllState): Map<String, Any> {
        val userInput = state.value(DataAgentSpec.Graph.StateKey.Input.USER_INPUT, "")
        val databaseId = state.value(DataAgentSpec.Graph.StateKey.Input.DATABASE_ID, "")
        val multiTurn = state.value(DataAgentSpec.Graph.StateKey.Input.MULTI_TURN_CONTEXT, "(无)")
        val beanOutputConverter: BeanOutputConverter<EvidenceQueryRewriteDTO> =
            BeanOutputConverter(
                EvidenceQueryRewriteDTO::class.java
            )
        val rewritePrompt = promptManager.evidenceQueryRewritePromptTemplate.render(
            mapOf(
                "latest_query" to userInput,
                "format" to beanOutputConverter.format,
                "multi_turn" to multiTurn

            )
        )
        logger.info { "Rewrite prompt: $rewritePrompt" }
        val rewriteResponse = ChatClient.create(chatModel)
            .prompt()
            .options(
                OpenAiChatOptions.builder()
                    .extraBody(mapOf("enable_thinking" to false))
                    .build()
            )
            .user(rewritePrompt)
            .call()
            .content() ?: throw IllegalArgumentException("Invalid rewrite response")
        logger.info { "Rewrite response: $rewriteResponse" }
        val convert = BeanOutputConverter(
            EvidenceQueryRewriteDTO::class.java
        ).convert(rewriteResponse) ?: throw IllegalArgumentException("Invalid rewrite response")
        val rewriteQuery = convert.standaloneQuery
        val terms = retrieveGlossaryKnowledge(rewriteQuery, databaseId)
        val knowledgeDocs = retrieveKnowledge(rewriteQuery, databaseId)
        val glossaryKnowledgeText = terms.joinToString("\n") {
            it.text
        }
        val ids =
            knowledgeDocs.mapNotNull { it.metadata.uuidOrNull(DataAgentSpec.Retrieval.DocumentMetadataKey.KNOWLEDGE_ID) }
                .distinct()
        val invalidKnowledgeIdCount = knowledgeDocs.size - ids.size
        if (invalidKnowledgeIdCount > 0) {
            logger.warn { "Skipped $invalidKnowledgeIdCount recalled knowledge docs due to invalid knowledgeId metadata" }
        }
        val questionKnowledgeText = questionKnowledgeRepository.findByIds(ids).joinToString("\n") {
            "来源：${it.databaseId} Q: ${it.question} A: ${it.answer}"
        }
        val glossaryPrompt =
            promptManager.businessKnowledgePromptTemplate.render(mapOf("businessKnowledge" to glossaryKnowledgeText.ifEmpty { "无" }))
        val knowledgePrompt =
            promptManager.agentKnowledgePromptTemplate.render(mapOf("agentKnowledge" to questionKnowledgeText.ifEmpty { "无" }))
        return mapOf(
            DataAgentSpec.Graph.StateKey.Recall.EVIDENCE to if (questionKnowledgeText.isEmpty() && glossaryKnowledgeText.isEmpty()) "无" else "$glossaryPrompt\n$knowledgePrompt",
            DataAgentSpec.Graph.StateKey.Recall.REWRITE_QUERY to rewriteQuery
        )
    }

    fun retrieveGlossaryKnowledge(question: String, databaseId: String): List<Document> {
        val builder = FilterExpressionBuilder()
        val filterExpression = builder.and(
            builder.eq(
                DataAgentSpec.Retrieval.DocumentMetadataKey.VECTOR_TYPE,
                DataAgentSpec.Retrieval.VectorType.GLOSSARY_KNOWLEDGE
            ),
            builder.eq(DataAgentSpec.Retrieval.DocumentMetadataKey.DATABASE_ID, databaseId)
        ).build()
        val request = SearchRequest.builder()
            .query(question)
            .filterExpression(filterExpression)
            .topK(4)
            .build()
        return vectorStore.similaritySearch(request)
    }

    fun retrieveKnowledge(question: String, databaseId: String): List<Document> {
        val builder = FilterExpressionBuilder()
        val expression = builder.and(
            builder.eq(
                DataAgentSpec.Retrieval.DocumentMetadataKey.VECTOR_TYPE,
                DataAgentSpec.Retrieval.VectorType.QUESTION_KNOWLEDGE
            ),
            builder.eq(DataAgentSpec.Retrieval.DocumentMetadataKey.DATABASE_ID, databaseId)
        ).build()
        val request = SearchRequest.builder()
            .query(question)
            .filterExpression(expression)
            .topK(4)
            .build()
        return vectorStore.similaritySearch(request)
    }
}
