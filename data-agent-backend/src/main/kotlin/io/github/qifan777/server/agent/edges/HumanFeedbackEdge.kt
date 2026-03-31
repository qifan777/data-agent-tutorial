package io.github.qifan777.server.agent.edges

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.StateGraph
import com.alibaba.cloud.ai.graph.action.EdgeAction
import io.github.qifan777.server.agent.DataAgentSpec

class HumanFeedbackEdge : EdgeAction {
    override fun apply(state: OverAllState): String {
        val nextNode = state.value(DataAgentSpec.Graph.StateKey.HumanReview.NEXT_NODE, StateGraph.END)
        return nextNode
    }

}