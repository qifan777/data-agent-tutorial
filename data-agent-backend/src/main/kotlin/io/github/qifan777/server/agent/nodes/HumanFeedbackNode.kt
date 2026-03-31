package io.github.qifan777.server.agent.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.StateGraph
import com.alibaba.cloud.ai.graph.action.NodeAction
import io.github.qifan777.server.agent.DataAgentSpec
import org.springframework.stereotype.Component

@Component
class HumanFeedbackNode : NodeAction {
    override fun apply(state: OverAllState): Map<String, Any> {
        val count = state.value(DataAgentSpec.Graph.StateKey.Planning.REPAIR_COUNT, 1)
        if (count >= 3) {
            return mapOf(DataAgentSpec.Graph.StateKey.HumanReview.NEXT_NODE to StateGraph.END)
        }
        val approved = state.value(DataAgentSpec.Graph.StateKey.HumanReview.CONFIRMATION_APPROVED, false)
        if (approved) {
            return mapOf(
                DataAgentSpec.Graph.StateKey.HumanReview.NEXT_NODE to DataAgentSpec.Graph.Node.PLAN_EXECUTION,
            )
        } else {
            val feedbackContent = state.value(DataAgentSpec.Graph.StateKey.HumanReview.CONFIRMATION_FEEDBACK, "")
            return mapOf(
                DataAgentSpec.Graph.StateKey.HumanReview.NEXT_NODE to DataAgentSpec.Graph.Node.PLANNER,
                DataAgentSpec.Graph.StateKey.Planning.REPAIR_COUNT to count + 1,
                DataAgentSpec.Graph.StateKey.HumanReview.CONFIRMATION_FEEDBACK to feedbackContent.ifEmpty { "Plan rejected by user" }
            )
        }
    }
}