package io.github.qifan777.server.agent.config

import com.alibaba.cloud.ai.graph.KeyStrategy
import com.alibaba.cloud.ai.graph.KeyStrategyFactory
import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.StateGraph
import com.alibaba.cloud.ai.graph.StateGraph.END
import com.alibaba.cloud.ai.graph.StateGraph.START
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async
import com.alibaba.cloud.ai.graph.serializer.StateSerializer
import com.alibaba.cloud.ai.graph.serializer.plain_text.jackson.SpringAIJacksonStateSerializer
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.github.qifan777.server.agent.DataAgentSpec
import io.github.qifan777.server.agent.edges.FeasibilityAssessmentEdge
import io.github.qifan777.server.agent.edges.HumanFeedbackEdge
import io.github.qifan777.server.agent.edges.PlanExecutorEdge
import io.github.qifan777.server.agent.nodes.*
import io.github.qifan777.server.graph.nodes.SqlExecuteNode
import org.babyfish.jimmer.jackson.v2.ImmutableModuleV2
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class GraphConfiguration {
    @Bean
    open fun serializer(): SpringAIJacksonStateSerializer {
        val serializer =
            SpringAIJacksonStateSerializer { OverAllState(it) }
        serializer.objectMapper().registerModules(JavaTimeModule())
        serializer.objectMapper().registerModules(ImmutableModuleV2())
        return serializer
    }

    @Bean
    open fun graph(
        evidenceRecallNode: EvidenceRecallNode,
        schemeReCallNode: SchemeReCallNode,
        tableRelationNode: TableRelationNode,
        feasibilityAssessmentNode: FeasibilityAssessmentNode,
        plannerNode: PlannerNode,
        serializer: StateSerializer, humanFeedbackNode: HumanFeedbackNode, planExecuteNode: PlanExecuteNode,
        sqlGeneratorNode: SqlGeneratorNode,
        sqlExecuteNode: SqlExecuteNode,
        pythonGeneratorNode: PythonGeneratorNode,
        pythonExecuteNode: PythonExecuteNode,
        pythonAnalyzeNode: PythonAnalyzeNode,
    ): StateGraph {
        val keyStrategyFactory = KeyStrategyFactory {
            val map = mutableMapOf<String, KeyStrategy>()
            map[DataAgentSpec.Graph.StateKey.Input.USER_INPUT] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Input.DATABASE_ID] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Input.MULTI_TURN_CONTEXT] = ReplaceStrategy()

            map[DataAgentSpec.Graph.StateKey.Recall.REWRITE_QUERY] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Recall.EVIDENCE] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Recall.TABLE_SCHEMA] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Recall.COLUMN_SCHEMA] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Recall.TABLE_RELATION] = ReplaceStrategy()

            map[DataAgentSpec.Graph.StateKey.Planning.PLAN] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Planning.REPAIR_COUNT] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Planning.NEXT_NODE] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Planning.CURRENT_STEP] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Planning.EXECUTION_OUTPUT] = ReplaceStrategy()

            map[DataAgentSpec.Graph.StateKey.HumanReview.CONFIRMATION_APPROVED] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.HumanReview.CONFIRMATION_FEEDBACK] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.HumanReview.NEXT_NODE] = ReplaceStrategy()

            map[DataAgentSpec.Graph.StateKey.Execution.FEASIBILITY_RESULT] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Execution.SQL_GENERATION_RESULT] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Execution.SQL_EXECUTION_RESULT] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Execution.PYTHON_GENERATION_RESULT] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Execution.PYTHON_EXECUTION_RESULT] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Execution.REPORT_RESULT] = ReplaceStrategy()
            map
        }
        return StateGraph(DataAgentSpec.GRAPH_NAME, keyStrategyFactory, serializer)
            .addNode(DataAgentSpec.Graph.Node.EVIDENCE_RECALL, node_async(evidenceRecallNode))
            .addNode(DataAgentSpec.Graph.Node.SCHEMA_RECALL, node_async(schemeReCallNode))
            .addNode(DataAgentSpec.Graph.Node.TABLE_RELATION, node_async(tableRelationNode))
            .addNode(DataAgentSpec.Graph.Node.FEASIBILITY_ASSESSMENT, node_async(feasibilityAssessmentNode))
            .addNode(DataAgentSpec.Graph.Node.PLANNER, node_async(plannerNode))
            .addNode(DataAgentSpec.Graph.Node.HUMAN_FEEDBACK, node_async(humanFeedbackNode))
            .addNode(DataAgentSpec.Graph.Node.PLAN_EXECUTION, node_async(planExecuteNode))
            .addNode(DataAgentSpec.Graph.Node.SQL_GENERATION, node_async(sqlGeneratorNode))
            .addNode(DataAgentSpec.Graph.Node.SQL_EXECUTION, node_async(sqlExecuteNode))
            .addNode(DataAgentSpec.Graph.Node.PYTHON_GENERATION, node_async(pythonGeneratorNode))
            .addNode(DataAgentSpec.Graph.Node.PYTHON_EXECUTION, node_async(pythonExecuteNode))
            .addNode(DataAgentSpec.Graph.Node.PYTHON_ANALYSIS, node_async(pythonAnalyzeNode))
            .addEdge(START, DataAgentSpec.Graph.Node.EVIDENCE_RECALL)
            .addEdge(DataAgentSpec.Graph.Node.EVIDENCE_RECALL, DataAgentSpec.Graph.Node.SCHEMA_RECALL)
            .addEdge(DataAgentSpec.Graph.Node.SCHEMA_RECALL, DataAgentSpec.Graph.Node.TABLE_RELATION)
            .addEdge(DataAgentSpec.Graph.Node.TABLE_RELATION, DataAgentSpec.Graph.Node.FEASIBILITY_ASSESSMENT)
            .addConditionalEdges(
                DataAgentSpec.Graph.Node.FEASIBILITY_ASSESSMENT,
                edge_async(FeasibilityAssessmentEdge()),
                mapOf(
                    DataAgentSpec.Graph.Node.PLANNER to DataAgentSpec.Graph.Node.PLANNER,
                    END to END,
                )
            )
            .addEdge(DataAgentSpec.Graph.Node.PLANNER, DataAgentSpec.Graph.Node.HUMAN_FEEDBACK)
            .addConditionalEdges(
                DataAgentSpec.Graph.Node.HUMAN_FEEDBACK, edge_async(HumanFeedbackEdge()),
                mapOf(
                    END to END,
                    DataAgentSpec.Graph.Node.PLAN_EXECUTION to DataAgentSpec.Graph.Node.PLAN_EXECUTION,
                    DataAgentSpec.Graph.Node.PLANNER to DataAgentSpec.Graph.Node.PLANNER
                )
            )
            .addConditionalEdges(
                DataAgentSpec.Graph.Node.PLAN_EXECUTION, edge_async(PlanExecutorEdge()),
                mapOf(
                    DataAgentSpec.Graph.Node.SQL_GENERATION to DataAgentSpec.Graph.Node.SQL_GENERATION,
                    DataAgentSpec.Graph.Node.PYTHON_GENERATION to DataAgentSpec.Graph.Node.PYTHON_GENERATION,
                    END to END,
                )
            )
            .addEdge(DataAgentSpec.Graph.Node.SQL_GENERATION, DataAgentSpec.Graph.Node.SQL_EXECUTION)
            .addEdge(DataAgentSpec.Graph.Node.SQL_EXECUTION, DataAgentSpec.Graph.Node.PLAN_EXECUTION)
            .addEdge(DataAgentSpec.Graph.Node.PYTHON_GENERATION, DataAgentSpec.Graph.Node.PYTHON_EXECUTION)
            .addEdge(DataAgentSpec.Graph.Node.PYTHON_EXECUTION, DataAgentSpec.Graph.Node.PYTHON_ANALYSIS)
            .addEdge(DataAgentSpec.Graph.Node.PYTHON_ANALYSIS, END)
    }
}
