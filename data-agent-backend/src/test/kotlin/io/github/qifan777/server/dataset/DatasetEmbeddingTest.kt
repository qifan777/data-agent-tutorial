package io.github.qifan777.server.dataset

import io.github.qifan777.server.agent.DataAgentSpec
import io.github.qifan777.server.agent.toDocument
import io.github.qifan777.server.dataset.knowledge.repository.GlossaryKnowledgeRepository
import io.github.qifan777.server.dataset.knowledge.repository.QuestionKnowledgeRepository
import io.github.qifan777.server.dataset.scheme.repository.DbColumnRepository
import io.github.qifan777.server.dataset.scheme.repository.DbTableRepository
import org.junit.jupiter.api.Test
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DatasetEmbeddingTest(
    @Autowired private val dbTableRepository: DbTableRepository,
    @Autowired private val dbColumnRepository: DbColumnRepository,
    @Autowired private val questionKnowledgeRepository: QuestionKnowledgeRepository,
    @Autowired private val glossaryKnowledgeRepository: GlossaryKnowledgeRepository,
    @Autowired private val vectorStore: VectorStore
) {
    @Test
    fun embeddingTest() {
        val documents = buildList {
            addAll(dbTableRepository.findByDatabaseId("california_schools").map { it.toDocument() })
            addAll(dbColumnRepository.findByDatabaseId("california_schools").map { it.toDocument() })
            addAll(questionKnowledgeRepository.findByDatabaseId("california_schools").map { it.toDocument() })
            addAll(glossaryKnowledgeRepository.findByDatabaseId("california_schools").map { it.toDocument() })
        }
        documents.chunked(10)
            .forEach {
                vectorStore.add(it)
            }
    }

    @Test
    fun retrieveTest() {
        val builder = FilterExpressionBuilder()
        val filterExpression = builder.and(
            builder.eq(
                DataAgentSpec.Retrieval.DocumentMetadataKey.VECTOR_TYPE,
                DataAgentSpec.Retrieval.VectorType.TABLE
            ),
            builder.eq(DataAgentSpec.Retrieval.DocumentMetadataKey.DATABASE_ID, "california_schools")
        ).build()


        val request = SearchRequest.builder()
            .query("What is the highest eligible free rate for K-12 students in the schools in Alameda County?")
            .filterExpression(filterExpression)
            .topK(5)
            .build()
        val documents = vectorStore.similaritySearch(request)
        documents.sortByDescending { it.score }
        logger.info { documents }

    }
}