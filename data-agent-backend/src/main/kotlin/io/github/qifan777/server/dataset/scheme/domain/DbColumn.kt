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

