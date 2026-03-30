package io.github.qifan777.server.dataset.scheme.domain

import io.github.qifan777.server.dataset.scheme.domain.dto.DbForeignKeySchemaView
import org.babyfish.jimmer.sql.*
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import java.util.*

@Entity
interface DbForeignKey {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID

    @Key
    @ManyToOne
    val sourceColumn: DbColumn

    @Key
    @ManyToOne
    val targetColumn: DbColumn
}

fun DbForeignKeySchemaView.toExpression(): String =
    "${sourceColumn.dbTable.name}.${sourceColumn.name} = ${targetColumn.dbTable.name}.${targetColumn.name}"
