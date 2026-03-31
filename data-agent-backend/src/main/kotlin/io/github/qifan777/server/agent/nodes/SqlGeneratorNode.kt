package io.github.qifan777.server.agent.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.qifan777.server.agent.DataAgentSpec
import io.github.qifan777.server.agent.model.Plan
import io.github.qifan777.server.agent.model.Schema
import io.github.qifan777.server.agent.prompt.PromptManager
import io.github.qifan777.server.shared.json.JsonUtil
import io.github.qifan777.server.shared.markdown.MarkdownParserUtil
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Component
import kotlin.jvm.java

private val logger = KotlinLogging.logger {}

@Component
class SqlGeneratorNode(private val chatModel: ChatModel, private val promptManager: PromptManager) : NodeAction {
    override fun apply(state: OverAllState): Map<String, Any> {
        val step = Plan.getCurrentStep(state)
        val instruction = step.toolParameters.instruction ?: throw RuntimeException("sql 生成步骤 instruction为空")
        val schemeDto =
            JsonUtil.fromJson(
                state.value(DataAgentSpec.Graph.StateKey.Recall.TABLE_RELATION, String::class.java).orElseThrow(),
                Schema::class.java
            )!!
        val rewriteQuery = state.value(DataAgentSpec.Graph.StateKey.Recall.REWRITE_QUERY, "")
        val evidence = state.value(DataAgentSpec.Graph.StateKey.Recall.EVIDENCE, "")
        val dialect = "mysql"
        val sqlPrompt = promptManager.newSqlGeneratorPromptTemplate.render(
            mapOf(
                "dialect" to dialect,
                "question" to rewriteQuery,
                "schema_info" to schemeDto.buildSchemePrompt(),
                "evidence" to evidence,
                "execution_description" to instruction
            )
        )
        val sql = ChatClient.create(chatModel)
            .prompt()
            .system(sqlPrompt)
            .options(
                OpenAiChatOptions.builder()
                    .extraBody(mapOf("enable_thinking" to false))
                    .build()
            )
            .call()
            .content()!!
        logger.info {
            "sql $sql"
        }
        return mapOf(DataAgentSpec.Graph.StateKey.Execution.SQL_GENERATION_RESULT to MarkdownParserUtil.extractRawText(sql))
    }
}
