package io.github.qifan777.server.agent.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.StateGraph
import com.alibaba.cloud.ai.graph.action.NodeAction
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.qifan777.server.agent.DataAgentSpec
import io.github.qifan777.server.agent.model.Plan
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class PlanExecuteNode : NodeAction {
    override fun apply(state: OverAllState): Map<String, Any> {
        val plan = Plan.getPlan(state)
        val currentStep = state.value(DataAgentSpec.Graph.StateKey.Planning.CURRENT_STEP, 1)
        val steps = plan.executionPlan
        if (currentStep > steps.size) {
            logger.info {
                "plan complete, current step is $currentStep, total step is ${steps.size}"
            }
            return mapOf(
                DataAgentSpec.Graph.StateKey.Planning.CURRENT_STEP to 1,
                DataAgentSpec.Graph.StateKey.Planning.NEXT_NODE to StateGraph.END,
            )
        }
        return mapOf(
            DataAgentSpec.Graph.StateKey.Planning.NEXT_NODE to steps[currentStep - 1].toolToUse,
        )
    }
}