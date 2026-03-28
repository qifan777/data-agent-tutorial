package io.github.qifan777.server.graph.edges

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.EdgeAction
import io.github.qifan777.server.graph.ToyGraphSpec
import org.springframework.stereotype.Component

@Component
class ToySceneBranchEdge : EdgeAction {
    override fun apply(state: OverAllState): String {
        return state.value(ToyGraphSpec.StateKey.SCENE, ToyGraphSpec.Scene.STUDY)
    }
}
