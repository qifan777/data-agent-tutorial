create database data_agent_tutorial;
-- 记得切换到 dataa_agent_tutorial数据库
-- 1. 创建 GlossaryKnowledge 表
CREATE TABLE glossary_knowledge
(
    id          UUID PRIMARY KEY,
    database_id VARCHAR(255) NOT NULL,
    term        VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL,
    synonyms    VARCHAR(255) -- Nullable, 因为代码中是 String?
);

-- 2. 创建 QuestionKnowledge 表
CREATE TABLE question_knowledge
(
    id          UUID PRIMARY KEY,
    database_id VARCHAR(255) NOT NULL,
    question    TEXT         NOT NULL,
    answer      TEXT         NOT NULL
);

-- 3. 创建 DbTable 表
CREATE TABLE db_table
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL,
    database_id VARCHAR(255) NOT NULL,
    -- DbTable 中 name 和 databaseId 都标记了 @Key，因此建立联合唯一索引
    CONSTRAINT uq_db_table_name_database UNIQUE (database_id, name)
);

-- 4. 创建 DbColumn 表
CREATE TABLE db_column
(
    id             UUID PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    type           VARCHAR(255) NOT NULL,
    description    TEXT         NOT NULL,
    is_primary_key BOOLEAN      NOT NULL,
    table_id       UUID         NOT NULL, -- 由 @JoinColumn(name = "table_id") 指定
    CONSTRAINT fk_db_column_table_id FOREIGN KEY (table_id) REFERENCES db_table (id) ON DELETE CASCADE,
    -- DbColumn 中 name 和 dbTable 都标记了 @Key
    CONSTRAINT uq_db_column_table_name UNIQUE (table_id, name)
);

-- 5. 创建 DbForeignKey 表
CREATE TABLE db_foreign_key
(
    id               UUID PRIMARY KEY,
    source_column_id UUID NOT NULL,
    target_column_id UUID NOT NULL,
    CONSTRAINT fk_db_fk_source_column FOREIGN KEY (source_column_id) REFERENCES db_column (id) ON DELETE CASCADE,
    CONSTRAINT fk_db_fk_target_column FOREIGN KEY (target_column_id) REFERENCES db_column (id) ON DELETE CASCADE,
    -- DbForeignKey 中 sourceColumn 和 targetColumn 都标记了 @Key
    CONSTRAINT uq_db_foreign_key_source_target UNIQUE (source_column_id, target_column_id)
);
