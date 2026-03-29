package io.github.qifan777.server.graph.edges

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.StateGraph
import com.alibaba.cloud.ai.graph.action.EdgeAction
import io.github.qifan777.server.graph.ToyGraphSpec
import org.springframework.stereotype.Component

@Component
class ToyConfirmationBranchEdge : EdgeAction {
    override fun apply(state: OverAllState): String {
        val approved = state.value(ToyGraphSpec.StateKey.CONFIRMATION_APPROVED, false)
        if (!approved) {
            return StateGraph.END
        }
        return state.value(ToyGraphSpec.StateKey.SCENE, ToyGraphSpec.Scene.STUDY)
    }
}
