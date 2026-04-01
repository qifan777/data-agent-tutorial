package io.github.qifan777.server.agent.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.qifan777.server.agent.DataAgentSpec
import io.github.qifan777.server.agent.model.Schema
import io.github.qifan777.server.agent.prompt.PromptManager
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class FeasibilityAssessmentNode(private val chatModel: ChatModel, private val promptManager: PromptManager) :
    NodeAction {
    override fun apply(state: OverAllState): Map<String, Any> {
        val rewriteQuery = state.value(DataAgentSpec.Graph.StateKey.Recall.REWRITE_QUERY, "")
        val schema = Schema.fromState(state)
        val evidence = state.value(DataAgentSpec.Graph.StateKey.Recall.EVIDENCE, "")
        val multiTurn = state.value(DataAgentSpec.Graph.StateKey.Input.MULTI_TURN_CONTEXT, "(无)")
        val prompt = promptManager.feasibilityAssessmentPromptTemplate.render(
            mapOf(
                "recalled_schema" to schema.buildSchemePrompt(),
                "evidence" to evidence,
                "canonical_query" to rewriteQuery,
                "multi_turn" to multiTurn,
            )
        )
        val feasibilityAssessment = ChatClient.create(chatModel)
            .prompt()
            .options(
                OpenAiChatOptions.builder()
                    .extraBody(mapOf("enable_thinking" to false))
                    .build()
            )
            .user(prompt)
            .call()
            .content() ?: throw RuntimeException("feasible assessment fail")
        logger.info {
            "feasibilityAssessment: $feasibilityAssessment"
        }
        return mapOf(DataAgentSpec.Graph.StateKey.Execution.FEASIBILITY_RESULT to feasibilityAssessment)
    }
}
