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
import io.github.qifan777.server.agent.nodes.*
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
        serializer: StateSerializer
    ): StateGraph {
        val keyStrategyFactory = KeyStrategyFactory {
            val map = mutableMapOf<String, KeyStrategy>()
            map[DataAgentSpec.Graph.StateKey.Recall.REWRITE_QUERY] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Recall.EVIDENCE] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Recall.TABLE_SCHEMA] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Recall.COLUMN_SCHEMA] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Recall.TABLE_RELATION] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Execution.FEASIBILITY_RESULT] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Planning.PLAN] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Input.DATABASE_ID] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Input.USER_INPUT] = ReplaceStrategy()
            map
        }
        return StateGraph(DataAgentSpec.GRAPH_NAME, keyStrategyFactory, serializer)
            .addNode(DataAgentSpec.Graph.Node.EVIDENCE_RECALL, node_async(evidenceRecallNode))
            .addNode(DataAgentSpec.Graph.Node.SCHEMA_RECALL, node_async(schemeReCallNode))
            .addNode(DataAgentSpec.Graph.Node.TABLE_RELATION, node_async(tableRelationNode))
            .addNode(DataAgentSpec.Graph.Node.FEASIBILITY_ASSESSMENT, node_async(feasibilityAssessmentNode))
            .addNode(DataAgentSpec.Graph.Node.PLANNER, node_async(plannerNode))
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
            .addEdge(DataAgentSpec.Graph.Node.PLANNER, END)
    }
}
