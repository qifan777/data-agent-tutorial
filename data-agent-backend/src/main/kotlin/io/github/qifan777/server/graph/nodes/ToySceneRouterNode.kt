package io.github.qifan777.server.graph.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import io.github.qifan777.server.graph.ToyGraphSpec
import org.springframework.stereotype.Component

@Component
class ToySceneRouterNode : NodeAction {
    override fun apply(state: OverAllState): Map<String, Any> {
        val input = state.value(ToyGraphSpec.StateKey.INPUT, "")
        val travelKeywords = listOf("旅游", "旅行", "出游", "攻略", "景点", "酒店", "美食", "周末去哪")
        val scene =
            if (travelKeywords.any { input.contains(it, ignoreCase = true) }) ToyGraphSpec.Scene.TRAVEL else ToyGraphSpec.Scene.STUDY
        val sceneLabel = if (scene == ToyGraphSpec.Scene.TRAVEL) "旅行攻略" else "学习计划"
        return mapOf(
            ToyGraphSpec.StateKey.SCENE to scene,
            ToyGraphSpec.StateKey.SCENE_LABEL to sceneLabel,
        )
    }
}
