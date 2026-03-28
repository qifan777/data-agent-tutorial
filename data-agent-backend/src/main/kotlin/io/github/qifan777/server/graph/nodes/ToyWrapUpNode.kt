package io.github.qifan777.server.graph.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import io.github.qifan777.server.graph.ToyGraphSpec
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Component

@Component
class ToyWrapUpNode(private val chatModel: ChatModel) : NodeAction {
    override fun apply(state: OverAllState): Map<String, Any> {
        val sceneLabel = state.value(ToyGraphSpec.StateKey.SCENE_LABEL, "内容规划")
        // 上游节点虽然返回的是 ChatResponse 流，但 graph 在状态里保存的是“流结束后的最终消息”，
        // 所以下游这里可以直接按 AssistantMessage 读取，再拿 text 给下一个 prompt 使用。
        val draft = state.value(ToyGraphSpec.StateKey.DRAFT, AssistantMessage::class.java).orElseThrow()
        val flux = ChatClient.create(chatModel)
            .prompt()
            .system("你是一个教程里的收尾节点。请把输入整理成更通俗的行动清单，控制在 3 到 5 条，每条一句话。")
            .user("请把这份${sceneLabel}整理成用户一看就能执行的清单：${draft.text}")
            .options(OpenAiChatOptions.builder().extraBody(mapOf("enable_thinking" to false)).build())
            .stream()
            .chatResponse()
        return mapOf(ToyGraphSpec.StateKey.FINAL_OUTPUT to flux)
    }
}
