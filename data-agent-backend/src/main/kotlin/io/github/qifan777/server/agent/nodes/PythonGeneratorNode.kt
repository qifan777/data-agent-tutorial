package io.github.qifan777.server.agent.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.qifan777.server.agent.DataAgentSpec
import io.github.qifan777.server.agent.model.Plan
import io.github.qifan777.server.agent.model.Schema
import io.github.qifan777.server.agent.prompt.PromptManager
import io.github.qifan777.server.graph.nodes.SqlExecuteNode
import io.github.qifan777.server.shared.json.JsonUtil
import io.github.qifan777.server.shared.markdown.MarkdownParserUtil
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class PythonGeneratorNode(private val chatModel: ChatModel, private val promptManager: PromptManager) : NodeAction {
    override fun apply(state: OverAllState): Map<String, Any> {
        logger.info { "apply PythonGeneratorNode" }
        val schemeDto = Schema.fromState(state)
        val result =
            state.value(DataAgentSpec.Graph.StateKey.Execution.SQL_EXECUTION_RESULT, SqlExecuteNode.Result::class.java).orElseThrow()
        val executionStep = Plan.getCurrentStep(state)
        val prompt = promptManager.pythonGeneratorPromptTemplate.render(
            mapOf(
                "python_memory" to "500",
                "python_timeout" to "500",
                "database_schema" to schemeDto.buildSchemePrompt(),
                "sample_input" to JsonUtil.toJson(result.resultSet.data),
                "plan_description" to JsonUtil.toJson(executionStep.toolParameters)
            )
        )
        val pythonCode = ChatClient.create(chatModel)
            .prompt()
            .system(prompt)
            .options(
                OpenAiChatOptions.builder()
                    .extraBody(mapOf("enable_thinking" to false))
                    .build()
            )
            .call()
            .content()
        return mapOf(DataAgentSpec.Graph.StateKey.Execution.PYTHON_GENERATION_RESULT to MarkdownParserUtil.extractRawText(pythonCode))
    }
}