package io.github.qifan777.server.graph.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import org.springframework.stereotype.Component

@Component
class ToyResumeNode : NodeAction {
    override fun apply(state: OverAllState): Map<String, Any> {
        return emptyMap()
    }
}
