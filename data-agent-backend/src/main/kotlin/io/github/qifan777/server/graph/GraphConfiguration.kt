package io.github.qifan777.server.graph

import com.alibaba.cloud.ai.graph.KeyStrategy
import com.alibaba.cloud.ai.graph.KeyStrategyFactory
import com.alibaba.cloud.ai.graph.StateGraph
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction
import io.github.qifan777.server.graph.edges.ToyConfirmationBranchEdge
import io.github.qifan777.server.graph.edges.ToySceneBranchEdge
import io.github.qifan777.server.graph.nodes.ToySceneRouterNode
import io.github.qifan777.server.graph.nodes.ToyStudyPlanNode
import io.github.qifan777.server.graph.nodes.ToyTravelPlanNode
import io.github.qifan777.server.graph.nodes.ToyWrapUpNode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class GraphConfiguration {
    @Bean
    open fun toyBranchStreamingGraph(
        toySceneRouterNode: ToySceneRouterNode,
        toySceneBranchEdge: ToySceneBranchEdge,
        toyTravelPlanNode: ToyTravelPlanNode,
        toyStudyPlanNode: ToyStudyPlanNode,
        toyWrapUpNode: ToyWrapUpNode,
    ): StateGraph {
        val keyStrategyFactory = KeyStrategyFactory {
            mutableMapOf(
                ToyGraphSpec.StateKey.INPUT to KeyStrategy.REPLACE,
                ToyGraphSpec.StateKey.SCENE to KeyStrategy.REPLACE,
                ToyGraphSpec.StateKey.SCENE_LABEL to KeyStrategy.REPLACE,
                ToyGraphSpec.StateKey.DRAFT to KeyStrategy.REPLACE,
                ToyGraphSpec.StateKey.FINAL_OUTPUT to KeyStrategy.REPLACE,
            )
        }

        return StateGraph(keyStrategyFactory)
            .addNode(ToyGraphSpec.Node.ROUTE, AsyncNodeAction.node_async(toySceneRouterNode))
            .addNode(ToyGraphSpec.Node.TRAVEL_PLAN, AsyncNodeAction.node_async(toyTravelPlanNode))
            .addNode(ToyGraphSpec.Node.STUDY_PLAN, AsyncNodeAction.node_async(toyStudyPlanNode))
            .addNode(ToyGraphSpec.Node.WRAP_UP, AsyncNodeAction.node_async(toyWrapUpNode))
            .addEdge(StateGraph.START, ToyGraphSpec.Node.ROUTE)
            .addConditionalEdges(
                ToyGraphSpec.Node.ROUTE,
                AsyncEdgeAction.edge_async(toySceneBranchEdge),
                mapOf(
                    ToyGraphSpec.Scene.TRAVEL to ToyGraphSpec.Node.CONFIRM,
                    ToyGraphSpec.Scene.STUDY to ToyGraphSpec.Node.CONFIRM,
                ),
            )
            .addEdge(ToyGraphSpec.Node.TRAVEL_PLAN, ToyGraphSpec.Node.WRAP_UP)
            .addEdge(ToyGraphSpec.Node.STUDY_PLAN, ToyGraphSpec.Node.WRAP_UP)
            .addEdge(ToyGraphSpec.Node.WRAP_UP, StateGraph.END)
    }
}
