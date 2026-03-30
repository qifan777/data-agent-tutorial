package io.github.qifan777.server.agent

import com.alibaba.cloud.ai.graph.OverAllState

object DataAgentSpec {
    const val GRAPH_NAME = "data-agent-main-graph"

    object Graph {
        object Node {
            const val EVIDENCE_RECALL = "EVIDENCE_RECALL_NODE"
            const val SCHEMA_RECALL = "SCHEME_RECALL_NODE"
            const val TABLE_RELATION = "TABLE_RELATION_NODE"
            const val FEASIBILITY_ASSESSMENT = "FEASIBILITY_ASSESSMENT_NODE"
            const val PLANNER = "PLANNER_NODE"
            const val HUMAN_FEEDBACK = "HUMAN_FEEDBACK_NODE"
            const val PLAN_EXECUTION = "PLAN_EXECUTE_NODE"
            const val SQL_GENERATION = "SQL_GENERATE_NODE"
            const val SQL_EXECUTION = "SQL_EXECUTE_NODE"
            const val PYTHON_GENERATION = "PYTHON_GENERATE_NODE"
            const val PYTHON_EXECUTION = "PYTHON_EXECUTE_NODE"
            const val PYTHON_ANALYSIS = "PYTHON_ANALYZE_NODE"
            const val REPORT_GENERATION = "REPORT_GENERATOR_NODE"
        }

        object StateKey {
            object Input {
                const val USER_INPUT = "input"
                const val DATABASE_ID = "databaseId"
                const val MULTI_TURN_CONTEXT = "MULTI_TURN_CONTEXT"
            }

            object Recall {
                const val REWRITE_QUERY = "REWRITE_QUERY"
                const val EVIDENCE = "EVIDENCE"
                const val COLUMN_SCHEMA = "COLUMN_SCHEME"
                const val TABLE_SCHEMA = "TABLE_SCHEME"
                const val TABLE_RELATION = "TABLE_RELATION_OUTPUT"
            }

            object Planning {
                const val PLAN = "PLANNER_NODE_OUTPUT"
                const val VALIDATION_ERROR = "PLAN_VALIDATION_ERROR"
                const val REPAIR_COUNT = "PLAN_REPAIR_COUNT"
                const val NEXT_NODE = "PLAN_NEXT_NODE"
                const val CURRENT_STEP = "PLAN_CURRENT_STEP"
                const val VALIDATION_STATUS = "PLAN_VALIDATION_STATUS"
                const val EXECUTION_OUTPUT = "PLAN_EXECUTE_NODE_OUTPUT"
            }

            object HumanReview {
                const val FEEDBACK = "HUMAN_FEEDBACK_NODE_OUTPUT"
                const val REVIEW_ENABLED = "HUMAN_REVIEW_ENABLED"
                const val NEXT_NODE = "HUMAN_NEXT_NODE"
            }

            object Execution {
                const val FEASIBILITY_RESULT = "FEASIBILITY_ASSESSMENT_NODE_OUTPUT"
                const val SQL_GENERATION_RESULT = "SQL_GENERATE_OUTPUT"
                const val SQL_EXECUTION_RESULT = "SQL_EXECUTE_OUTPUT"
                const val PYTHON_GENERATION_RESULT = "PYTHON_GENERATE_NODE_OUTPUT"
                const val PYTHON_EXECUTION_RESULT = "PYTHON_EXECUTE_NODE_OUTPUT"
                const val REPORT_RESULT = "REPORT_GENERATOR_NODE_OUTPUT"
            }
        }

        fun stringValue(state: OverAllState, key: String): String {
            return state.value<String>(key).orElseThrow { IllegalStateException("State key not found: $key") }
        }
    }

    object Retrieval {
        object DocumentMetadataKey {
            const val TABLE_ID = "tableId"
            const val COLUMN_ID = "columnId"
            const val KNOWLEDGE_ID = "knowledgeId"
            const val DATABASE_ID = "databaseId"
            const val BUSINESS_TERM_ID = "businessTermId"
            const val VECTOR_TYPE = "vectorType"
        }

        object VectorType {
            const val QUESTION_KNOWLEDGE = "questionKnowledge"
            const val GLOSSARY_KNOWLEDGE = "glossaryKnowledge"
            const val COLUMN = "column"
            const val TABLE = "table"
        }
    }

    object PromptName {
        const val INTENT_RECOGNITION = "intent-recognition"
        const val EVIDENCE_QUERY_REWRITE = "evidence-query-rewrite"
        const val AGENT_KNOWLEDGE = "agent-knowledge"
        const val QUERY_ENHANCEMENT = "query-enhancement"
        const val FEASIBILITY_ASSESSMENT = "feasibility-assessment"
        const val MIX_SELECTOR = "mix-selector"
        const val SEMANTIC_CONSISTENCY = "semantic-consistency"
        const val SQL_GENERATION = "new-sql-generate"
        const val PLANNER = "planner"
        const val REPORT_GENERATION = "report-generator-plain"
        const val SQL_ERROR_FIXER = "sql-error-fixer"
        const val PYTHON_GENERATION = "python-generator"
        const val PYTHON_ANALYSIS = "python-analyze"
        const val BUSINESS_KNOWLEDGE = "business-knowledge"
        const val SEMANTIC_MODEL = "semantic-model"
        const val JSON_FIX = "json-fix"
        const val DATA_VIEW_ANALYZE = "data-view-analyze"
    }


}
