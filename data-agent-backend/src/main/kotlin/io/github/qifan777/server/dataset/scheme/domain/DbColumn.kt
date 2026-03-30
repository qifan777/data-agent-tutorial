package io.github.qifan777.server.dataset.scheme.domain

import io.github.qifan777.server.agent.DataAgentSpec
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import org.springframework.ai.document.Document
import java.util.UUID

@Entity
interface DbColumn  {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID
    @Key
    val name: String
    val type: String
    val description: String
    val isPrimaryKey: Boolean

    @JoinColumn(name = "table_id")
    @Key
    @ManyToOne
    val dbTable: DbTable
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
