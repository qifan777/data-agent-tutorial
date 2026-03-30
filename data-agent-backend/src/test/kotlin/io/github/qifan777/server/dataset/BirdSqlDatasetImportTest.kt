package io.github.qifan777.server.dataset

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.qifan777.server.dataset.knowledge.domain.GlossaryKnowledge
import io.github.qifan777.server.dataset.knowledge.domain.QuestionKnowledge
import io.github.qifan777.server.dataset.knowledge.repository.GlossaryKnowledgeRepository
import io.github.qifan777.server.dataset.knowledge.repository.QuestionKnowledgeRepository
import io.github.qifan777.server.dataset.scheme.domain.DbColumn
import io.github.qifan777.server.dataset.scheme.domain.DbForeignKey
import io.github.qifan777.server.dataset.scheme.domain.DbTable
import io.github.qifan777.server.dataset.scheme.repository.DbColumnRepository
import io.github.qifan777.server.dataset.scheme.repository.DbForeignKeyRepository
import io.github.qifan777.server.dataset.scheme.repository.DbTableRepository
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource

val logger = KotlinLogging.logger {}

@SpringBootTest
class BirdSqlDatasetImportTest(
    @Autowired private val glossaryKnowledgeRepository: GlossaryKnowledgeRepository,
    @Autowired private val questionKnowledgeRepository: QuestionKnowledgeRepository,
    @Autowired private val dbTableRepository: DbTableRepository,
    @Autowired private val dbForeignKeyRepository: DbForeignKeyRepository,
    @Autowired private val dbColumnRepository: DbColumnRepository
) {
    @Test
    fun createKnowledgeTest() {
        val objectMapper = jacksonObjectMapper()
        val birdQuestionDtos: List<BirdQuestion> = objectMapper.readValue(
            ClassPathResource("dev_20240627/dev.json").inputStream,
            object : TypeReference<List<BirdQuestion>>() {}
        )
        println("总共读取了 ${birdQuestionDtos.size} 个问题")
        glossaryKnowledgeRepository.deleteAll()
        questionKnowledgeRepository.deleteAll()
        birdQuestionDtos.filter { it.evidence.isNotEmpty() }
            .distinctBy { it.evidence }
            .forEach { question ->
                logger.info { question }
                glossaryKnowledgeRepository.save(GlossaryKnowledge {
                    databaseId = question.dbId
                    term = ""
                    description = question.evidence
                }, mode = SaveMode.INSERT_ONLY)
            }
        birdQuestionDtos.forEach { question ->
            questionKnowledgeRepository.save(QuestionKnowledge {
                databaseId = question.dbId
                this.question = question.question
                this.answer = question.sql
            }, mode = SaveMode.INSERT_ONLY)
        }
    }

    @Test
    fun createSchemeTest() {
        val objectMapper = jacksonObjectMapper()
        val birdTableDtos: List<BirdTable> = objectMapper.readValue(
            ClassPathResource("dev_20240627/dev_tables.json").inputStream,
            object : TypeReference<List<BirdTable>>() {}
        )

        val tablesToSave = mutableListOf<DbTable>()
        // 用于保存列的构建逻辑，等待 Table 获得持久化后的 ID 再执行
        val columnBuilders = mutableListOf<(DbTable) -> List<DbColumn>>()
        val pendingForeignKeys = mutableListOf<PendingForeignKey>()

        birdTableDtos.forEach { databaseTable ->
            databaseTable.tableNamesOriginal.forEachIndexed { index, tableName ->
                // 1. 只构建 DbTable，不包含 columns
                tablesToSave.add(
                    DbTable {
                        name = tableName
                        description = databaseTable.tableNames[index]
                        databaseId = databaseTable.dbId
                    }
                )

                // 2. 将 Column 的构建逻辑挂起，入参为持久化后的 savedTable
                columnBuilders.add { savedTable ->
                    databaseTable.columnNamesOriginal
                        .mapIndexedNotNull { columnIndex, list ->
                            if (list[0] == index && (list[1] as String).isNotEmpty()) {
                                DbColumn {
                                    name = list[1] as String
                                    description = databaseTable.columnNames[columnIndex][1] as String
                                    type = databaseTable.columnTypes[columnIndex]
                                    isPrimaryKey = isPrimaryKey(databaseTable, columnIndex)
                                    // 关键：主动维护 ManyToOne 关系，关联到已保存的 Table
                                    dbTable = savedTable
                                }
                            } else null
                        }
                }
            }

            val dbForeignKeys = databaseTable.foreignKeys.mapNotNull { pair ->
                if (pair.size < 2) {
                    logger.warn { "跳过格式错误外键: dbId=${databaseTable.dbId}, pair=$pair" }
                    return@mapNotNull null
                }

                val sourceColumnInfo = databaseTable.columnNamesOriginal.getOrNull(pair[0])
                val targetColumnInfo = databaseTable.columnNamesOriginal.getOrNull(pair[1])
                if (sourceColumnInfo == null || targetColumnInfo == null) {
                    logger.warn { "跳过无效外键: dbId=${databaseTable.dbId}, pair=$pair" }
                    null
                } else {
                    val sourceTableIndex = sourceColumnInfo[0] as Int
                    val targetTableIndex = targetColumnInfo[0] as Int
                    val sourceTableName = databaseTable.tableNamesOriginal.getOrNull(sourceTableIndex)
                    val targetTableName = databaseTable.tableNamesOriginal.getOrNull(targetTableIndex)
                    if (sourceTableName == null || targetTableName == null) {
                        logger.warn { "跳过无效外键(表索引): dbId=${databaseTable.dbId}, pair=$pair" }
                        null
                    } else {
                        PendingForeignKey(
                            databaseId = databaseTable.dbId,
                            sourceTableName = sourceTableName,
                            sourceColumnName = sourceColumnInfo[1] as String,
                            targetTableName = targetTableName,
                            targetColumnName = targetColumnInfo[1] as String
                        )
                    }
                }
            }
            pendingForeignKeys.addAll(dbForeignKeys)
        }

        logger.info { "准备保存 ${tablesToSave.size} 张表" }
        // 步骤 A: 独立保存所有的表
        val savedTables = dbTableRepository.saveAll(tablesToSave)

        // 步骤 B: 利用刚才生成的 savedTables（带自增ID/UUID），来生成对应的列
        val columnsToSave = savedTables.flatMapIndexed { index, savedTable ->
            columnBuilders[index](savedTable)
        }

        logger.info { "准备保存 ${columnsToSave.size} 个列" }
        // 步骤 C: 独立保存所有的列
        val savedColumns = dbColumnRepository.saveAll(columnsToSave)

        // 步骤 D: 构建外键查找所需的 Map
        // 这里因为 savedColumns 中的 dbTable 是完整的对象，可以直接获取 databaseId 和 name
        val columnMap = savedColumns.associateBy { column ->
            Triple(column.dbTable.databaseId, column.dbTable.name, column.name)
        }

        val foreignKeys = pendingForeignKeys.mapNotNull { pending ->
            val source = columnMap[Triple(pending.databaseId, pending.sourceTableName, pending.sourceColumnName)]
            val target = columnMap[Triple(pending.databaseId, pending.targetTableName, pending.targetColumnName)]
            if (source == null || target == null) {
                logger.warn { "跳过无法匹配已保存列的外键: $pending" }
                null
            } else {
                DbForeignKey {
                    sourceColumn = source
                    targetColumn = target
                }
            }
        }
        logger.info { "读取了${foreignKeys.size}个外键" }
        dbForeignKeyRepository.saveAll(foreignKeys)
    }

    fun isPrimaryKey(birdTableDto: BirdTable, columnIndex: Int): Boolean {
        return birdTableDto.primaryKeys.flatMap {
            if (it is Int) {
                return@flatMap listOf(it)
            } else if (it is List<*>) {
                return@flatMap it
            }
            listOf()
        }.map {
            it as Int
        }.contains(columnIndex)
    }

    data class PendingForeignKey(
        val databaseId: String,
        val sourceTableName: String,
        val sourceColumnName: String,
        val targetTableName: String,
        val targetColumnName: String
    )

    data class BirdTable(
        @get:JsonProperty("db_id")
        val dbId: String,

        @get:JsonProperty("table_names_original")
        val tableNamesOriginal: List<String>,

        @get:JsonProperty("table_names")
        val tableNames: List<String>,

        @get:JsonProperty("column_names_original")
        val columnNamesOriginal: List<List<Any>>,

        @get:JsonProperty("column_names")
        val columnNames: List<List<Any>>,

        @get:JsonProperty("column_types")
        val columnTypes: List<String>,

        @get:JsonProperty("primary_keys")
        val primaryKeys: List<Any>,

        @get:JsonProperty("foreign_keys")
        val foreignKeys: List<List<Int>>
    )

    data class BirdQuestion(
        @get:JsonProperty("question_id")
        val questionId: Int,

        @get:JsonProperty("db_id")
        val dbId: String,

        @get:JsonProperty("question")
        val question: String,

        @get:JsonProperty("evidence")
        val evidence: String,

        @get:JsonProperty("SQL")
        val sql: String,

        @get:JsonProperty("difficulty")
        val difficulty: String
    )
}