package io.github.qifan777.server.dataset.scheme.domain

import io.github.qifan777.server.agent.DataAgentSpec
import org.babyfish.jimmer.sql.*
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import org.springframework.ai.document.Document
import java.util.*

@Entity
interface DbTable {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID

    @Key
    val name: String
    val description: String

    @Key
    val databaseId: String

    @OneToMany(mappedBy = "dbTable")
    val columns: List<DbColumn>
}


