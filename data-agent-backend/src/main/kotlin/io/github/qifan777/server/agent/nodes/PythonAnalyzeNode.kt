package io.github.qifan777.server.agent.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import io.github.qifan777.server.agent.DataAgentSpec
import io.github.qifan777.server.agent.prompt.PromptManager
import io.github.qifan777.server.shared.python.PythonExecutionResult
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Component

@Component
class PythonAnalyzeNode(private val chatModel: ChatModel, private val promptManager: PromptManager) : NodeAction {
    override fun apply(state: OverAllState): Map<String, Any> {
        val rewriteQuery = state.value(DataAgentSpec.Graph.StateKey.Recall.REWRITE_QUERY, "")
        val pythonOutput = state.value(
            DataAgentSpec.Graph.StateKey.Execution.PYTHON_EXECUTION_RESULT,
            PythonExecutionResult::class.java
        ).orElseThrow()
        val step = state.value(DataAgentSpec.Graph.StateKey.Planning.CURRENT_STEP, 1)
        val map = state.value<MutableMap<String, String>>(DataAgentSpec.Graph.StateKey.Planning.EXECUTION_OUTPUT)
            .orElseThrow()
        val prompt = promptManager.pythonAnalyzePromptTemplate.render(
            mapOf(
                "python_output" to pythonOutput,
                "user_query" to rewriteQuery
            )
        )
        val analyze = ChatClient.create(chatModel)
            .prompt()
            .options(OpenAiChatOptions.builder().extraBody(mapOf("enable_thinking" to false)).build())
            .user(prompt)
            .call()
            .content() ?: ""
        map["step_${step}_analysis"] = analyze
        return mapOf(
            DataAgentSpec.Graph.StateKey.Planning.EXECUTION_OUTPUT to map,
            DataAgentSpec.Graph.StateKey.Planning.CURRENT_STEP to step + 1
        )
    }
}
