package io.github.qifan777.server.agent.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.qifan777.server.agent.DataAgentSpec
import io.github.qifan777.server.agent.model.Schema
import io.github.qifan777.server.agent.prompt.PromptManager
import io.github.qifan777.server.agent.uuidOrNull
import io.github.qifan777.server.dataset.scheme.domain.copy
import io.github.qifan777.server.dataset.scheme.domain.dto.DbForeignKeySchemaView
import io.github.qifan777.server.dataset.scheme.domain.dto.DbTableSchemaView
import io.github.qifan777.server.dataset.scheme.repository.DbForeignKeyRepository
import io.github.qifan777.server.dataset.scheme.repository.DbTableRepository
import io.github.qifan777.server.shared.json.JsonUtil
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.document.Document
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Component
import java.util.*
import kotlin.math.abs

private val logger = KotlinLogging.logger {}

@Component
class TableRelationNode(
    private val objectMapper: ObjectMapper,
    private val chatModel: ChatModel,
    private val dbForeignKeyRepository: DbForeignKeyRepository,
    private val dbTableRepository: DbTableRepository,
    private val promptManager: PromptManager
) : NodeAction {
    private val baseHighSimilarityThreshold = 0.6
    private val targetTableCount = 4

    override fun apply(state: OverAllState): Map<String, Any> {
        val rewriteQuery = state.value(DataAgentSpec.Graph.StateKey.Recall.REWRITE_QUERY, "");
        val evidence = state.value(DataAgentSpec.Graph.StateKey.Recall.EVIDENCE, "")
        val tableDocuments = state.value(DataAgentSpec.Graph.StateKey.Recall.TABLE_SCHEMA, listOf<Document>())
        val columnDocuments = state.value(DataAgentSpec.Graph.StateKey.Recall.COLUMN_SCHEMA, listOf<Document>())
        val databaseId = state.value(DataAgentSpec.Graph.StateKey.Input.DATABASE_ID, "")
        val tableScores = tableDocuments
            .mapNotNull { document ->
                val tableId = document.metadata.uuidOrNull(DataAgentSpec.Retrieval.DocumentMetadataKey.TABLE_ID)
                    ?: return@mapNotNull null
                val score = document.score ?: return@mapNotNull null
                tableId to score
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, scores) -> scores.maxOrNull() ?: 0.0 }
        val columnScores = columnDocuments
            .mapNotNull { document ->
                val tableId = document.metadata.uuidOrNull(DataAgentSpec.Retrieval.DocumentMetadataKey.TABLE_ID)
                    ?: return@mapNotNull null
                val columnId =
                    document.metadata.uuidOrNull(DataAgentSpec.Retrieval.DocumentMetadataKey.COLUMN_ID)
                        ?: return@mapNotNull null
                val score = document.score ?: return@mapNotNull null
                ColumnScore(tableId, columnId, score)
            }
        // 动态thread hold
        val thresholdSelection = selectThreshold(tableScores, columnScores)
        val tableIds = thresholdSelection.tableIds
        val highSimilarityColumnIdsByTable = thresholdSelection.highSimilarityColumnIdsByTable
        val foreignKeys = dbForeignKeyRepository.findByDatabaseId(databaseId)
        val relatedForeignKeys = foreignKeys.filter {
            tableIds.contains(it.sourceColumn.dbTable.id) || tableIds.contains(it.targetColumn.dbTable.id)
        }
        val foreignKeyRelatedTableIds = relatedForeignKeys
            .flatMap { listOf(it.sourceColumn.dbTable.id, it.targetColumn.dbTable.id) }
            .toSet()
        val finalTableIds = linkedSetOf<UUID>().apply {
            addAll(tableIds)
            addAll(foreignKeyRelatedTableIds)
        }.toList()
        val foreignKeyColumnIdsByTable = relatedForeignKeys
            .flatMap { listOf(it.sourceColumn, it.targetColumn) }
            .groupBy({ it.dbTable.id }, { it.id })
            .mapValues { (_, rows) -> rows.toSet() }

        val tables = dbTableRepository.findByIds(finalTableIds, DbTableRepository.FETCHER).map { table ->
            val highSimilarityColumnIds = highSimilarityColumnIdsByTable[table.id] ?: emptySet()
            val foreignKeyColumnIds = foreignKeyColumnIdsByTable[table.id] ?: emptySet()
            val selectedColumnIds = (highSimilarityColumnIds + foreignKeyColumnIds).toSet()
            table.copy {
                columns = if (selectedColumnIds.isEmpty()) {
                    table.columns
                } else {
                    table.columns.filter {
                        selectedColumnIds.contains(it.id)
                    }
                }
            }
        }

        logger.info {
            "table relation recall merged tables: direct=${tableScores.keys.size}, " +
                    "column=${thresholdSelection.columnTableScoreKeys}, " +
                    "highTable=${thresholdSelection.highSimilarityTableCount}, " +
                    "highColumn=${thresholdSelection.highSimilarityColumnTableCount}, " +
                    "threshold=${thresholdSelection.threshold}, " +
                    "fkExpanded=${foreignKeyRelatedTableIds.size}, merged=${finalTableIds.size}"
        }

        val prompt = promptManager.mixSelectorPromptTemplate.render(
            mapOf(
                "schema_info" to Schema(
                    databaseId,
                    tables.map { DbTableSchemaView(it) },
                    relatedForeignKeys.map { DbForeignKeySchemaView(it) },
                ).buildSchemePrompt(),
                "question" to rewriteQuery,
                "evidence" to evidence,
            )
        ).toString()
        logger.info {
            "mix select prompt $prompt"
        }
        val filterResult = ChatClient.create(chatModel)
            .prompt()
            .options(
                OpenAiChatOptions.builder()
                    .extraBody(mapOf("enable_thinking" to false))
                    .build()
            )
            .user(prompt)
            .call()
            .content()
            ?: throw RuntimeException("mix select fail")
        val filterTableNames = objectMapper.readValue(filterResult, object : TypeReference<List<String>>() {})
        // 一定要关联查询出columns，内部定义了fetcher
        val filterTables = dbTableRepository.findByDatabaseIdAndNames(databaseId, filterTableNames)
        val filterForeignKeys = foreignKeys.filter {
            filterTableNames.contains(it.targetColumn.dbTable.name) || filterTableNames.contains(it.sourceColumn.dbTable.name)
        }
        logger.info {
            "mix select filter tables $filterResult"
        }
        return mapOf(
            DataAgentSpec.Graph.StateKey.Recall.TABLE_RELATION to JsonUtil.toJson(
                Schema(
                    databaseId,
                    filterTables.map { DbTableSchemaView(it) },
                    filterForeignKeys.map { DbForeignKeySchemaView(it) },
                )
            )!!
        )
    }

    private data class ColumnScore(
        val tableId: UUID,
        val columnId: UUID,
        val score: Double
    )

    private data class ThresholdSelection(
        val threshold: Double,
        val tableIds: List<UUID>,
        val highSimilarityColumnIdsByTable: Map<UUID, Set<UUID>>,
        val highSimilarityTableCount: Int,
        val highSimilarityColumnTableCount: Int,
        val columnTableScoreKeys: Int
    )

    private fun selectThreshold(tableScores: Map<UUID, Double>, columnScores: List<ColumnScore>): ThresholdSelection {
        fun buildSelection(threshold: Double): ThresholdSelection {
            val tableScoreFromColumnRecall = columnScores
                .groupBy({ it.tableId }, { it.score })
                .mapValues { (_, scores) -> scores.maxOrNull() ?: 0.0 }
            val highSimilarityTableIds = tableScores
                .filterValues { it >= threshold }
                .keys
            val highSimilarityColumnScores = columnScores.filter { it.score >= threshold }
            val highSimilarityColumnTableIds = highSimilarityColumnScores.map { it.tableId }.toSet()
            val highSimilarityColumnIdsByTable = highSimilarityColumnScores
                .groupBy { it.tableId }
                .mapValues { (_, rows) -> rows.map { it.columnId }.toSet() }
            val tableIds = linkedSetOf<UUID>().apply {
                addAll(highSimilarityTableIds)
                addAll(highSimilarityColumnTableIds)
                if (isEmpty()) {
                    addAll(tableScores.entries.sortedByDescending { it.value }.take(1).map { it.key })
                    addAll(tableScoreFromColumnRecall.entries.sortedByDescending { it.value }.take(1).map { it.key })
                }
            }.toList()

            return ThresholdSelection(
                threshold = threshold,
                tableIds = tableIds,
                highSimilarityColumnIdsByTable = highSimilarityColumnIdsByTable,
                highSimilarityTableCount = highSimilarityTableIds.size,
                highSimilarityColumnTableCount = highSimilarityColumnTableIds.size,
                columnTableScoreKeys = tableScoreFromColumnRecall.keys.size
            )
        }

        var best = buildSelection(baseHighSimilarityThreshold)
        if (best.tableIds.size <= targetTableCount) {
            return best
        }

        for (i in 1..29) {
            val threshold = (baseHighSimilarityThreshold + i * 0.01).coerceAtMost(0.99)
            val candidate = buildSelection(threshold)
            val candidateDiff = abs(targetTableCount - candidate.tableIds.size)
            val bestDiff = abs(targetTableCount - best.tableIds.size)
            if (candidateDiff < bestDiff || (candidateDiff == bestDiff && candidate.threshold > best.threshold)) {
                best = candidate
            }
        }
        return best
    }

}
