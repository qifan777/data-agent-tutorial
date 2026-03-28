package io.github.qifan777.server.graph.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import io.github.qifan777.server.graph.ToyGraphSpec
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Component

@Component
class ToyStudyPlanNode(private val chatModel: ChatModel) : NodeAction {
    override fun apply(state: OverAllState): Map<String, Any> {
        val input = state.value(ToyGraphSpec.StateKey.INPUT, "")
        // 这里先返回 Spring AI 的 ChatResponse 流，graph 会在节点执行完成后把最终结果收敛成 AssistantMessage 存回 state["draft"]。
        val flux = ChatClient.create(chatModel)
            .prompt()
            .system("你是一个适合教程演示的学习教练。输出要口语化、结构清晰，分成目标拆解、今日安排、避坑提醒三部分。")
            .user("请根据这段需求给我一个简短学习计划：$input")
            .options(OpenAiChatOptions.builder().extraBody(mapOf("enable_thinking" to false)).build())
            .stream()
            .chatResponse()
        return mapOf(ToyGraphSpec.StateKey.DRAFT to flux)
    }
}
