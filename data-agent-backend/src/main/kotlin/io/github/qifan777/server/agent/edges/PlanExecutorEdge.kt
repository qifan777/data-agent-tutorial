package io.github.qifan777.server.agent.edges

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.StateGraph.END
import com.alibaba.cloud.ai.graph.action.EdgeAction
import io.github.qifan777.server.agent.DataAgentSpec

class PlanExecutorEdge : EdgeAction {
    override fun apply(state: OverAllState): String {
        val nextNode = state.value(DataAgentSpec.Graph.StateKey.Planning.NEXT_NODE, END)
        if (nextNode == END) {
            return END
        }
        return nextNode
    }
}