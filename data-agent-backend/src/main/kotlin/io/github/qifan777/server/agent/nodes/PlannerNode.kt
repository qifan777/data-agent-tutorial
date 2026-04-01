package io.github.qifan777.server.agent.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import io.github.qifan777.server.agent.DataAgentSpec
import io.github.qifan777.server.agent.model.Plan
import io.github.qifan777.server.agent.model.Schema
import io.github.qifan777.server.agent.prompt.PromptManager
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Component

@Component
class PlannerNode(private val chatModel: ChatModel, private val promptManager: PromptManager) : NodeAction {
    override fun apply(state: OverAllState): Map<String, Any> {
        val rewriteQuery = state.value(DataAgentSpec.Graph.StateKey.Recall.REWRITE_QUERY, "")
        val schemeDto = Schema.fromState(state)
        val feedbackContent = state.value(DataAgentSpec.Graph.StateKey.HumanReview.CONFIRMATION_FEEDBACK, "");
        val schemePrompt = schemeDto.buildSchemePrompt()
        val evidence = state.value(DataAgentSpec.Graph.StateKey.Recall.EVIDENCE, "")
        val beanOutputConverter = BeanOutputConverter(Plan::class.java)
        val prompt = promptManager.plannerPromptTemplate.render(
            mapOf(
                "user_question" to rewriteQuery,
                "schema" to schemePrompt,
                "evidence" to evidence,
                "semantic_model" to "",
                "plan_validation_error" to feedbackContent,
                "format" to beanOutputConverter.format
            )
        )
        val plan = ChatClient.create(chatModel)
            .prompt()
            .options(OpenAiChatOptions.builder().extraBody(mapOf("enable_thinking" to false)).build())
            .user(prompt)
            .call()
            .content()
        return mapOf(DataAgentSpec.Graph.StateKey.Planning.PLAN to plan!!)
    }
}