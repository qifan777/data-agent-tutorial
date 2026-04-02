package io.github.qifan777.server.agent.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.qifan777.server.agent.DataAgentSpec
import io.github.qifan777.server.agent.model.Plan
import io.github.qifan777.server.agent.prompt.PromptManager
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

private val logger = KotlinLogging.logger {}

@Component
class ReportGeneratorNode(private val chatModel: ChatModel, private val promptManager: PromptManager) : NodeAction {
    companion object {
        val cleanJsonExample = """
			{
			    "title": { "text": "月度销售额" },
			    "tooltip": { "trigger": "axis" },
			    "xAxis": { "type": "category", "data": ["1月", "2月"] },
			    "yAxis": { "type": "value" },
			    "series": [
			        { "type": "bar", "data": [120, 200] }
			    ]
			}
			""".trimIndent()
    }

    override fun apply(state: OverAllState): Map<String, Any> {
        logger.info { "apply PythonGeneratorNode" }
        val rewriteQuery = state.value(DataAgentSpec.Graph.StateKey.Recall.REWRITE_QUERY, "")
        val plan = Plan.getPlan(state)
        val result = state.value<Map<String, String>>(DataAgentSpec.Graph.StateKey.Planning.EXECUTION_OUTPUT).orElseThrow()
        val summaryAndRecommendations = Plan.getCurrentStep(state).toolParameters.summaryAndRecommendations!!
        return mapOf(
            DataAgentSpec.Graph.StateKey.Execution.REPORT_RESULT to generateReport(
                rewriteQuery,
                plan,
                result,
                summaryAndRecommendations
            )
        )
    }

    private fun generateReport(
        userInput: String, plan: Plan,
        executionResults: Map<String, String>,
        summaryAndRecommendations: String
    ): Flux<ChatResponse> {
        val userRequirementsAndPlan = buildUserRequirementsAndPlan(userInput, plan)
        val analysisStepsAndData = buildAnalysisStepsAndData(plan, executionResults)
        val reportPrompt: String = promptManager.reportGeneratorPlainPromptTemplate
            .render(
                mapOf(
                    "user_requirements_and_plan" to userRequirementsAndPlan,
                    "analysis_steps_and_data" to analysisStepsAndData,
                    "summary_and_recommendations" to summaryAndRecommendations,
                    "json_example" to cleanJsonExample,
                    "optimization_section" to ""
                )
            )
        logger.info { "Report Node Prompt: \n $reportPrompt \n" }
        return ChatClient.create(chatModel)
            .prompt()
            .options(OpenAiChatOptions.builder().extraBody(mapOf("enable_thinking" to false)).build())
            .user(reportPrompt)
            .stream()
            .chatResponse()
    }

    /**
     * 构建用户需求和计划描述。
     */
    private fun buildUserRequirementsAndPlan(userInput: String, plan: Plan): String {
        return buildString {
            append("## 用户原始需求\n")
            append(userInput).append("\n\n")

            append("## 执行计划概述\n")
            append("**思考过程**: ").append(plan.thoughtProcess).append("\n\n")

            append("## 详细执行步骤\n")
            plan.executionPlan.forEachIndexed { index, step ->
                append("### 步骤 ${index + 1}: 步骤编号 ${step.step}\n")
                append("**工具**: ${step.toolToUse}\n")
                step.toolParameters.let { params ->
                    append("**参数描述**: ${params.instruction}\n")
                }
                append("\n")
            }
        }
    }

    /**
     * 构建分析步骤和数据结果描述。
     */
    private fun buildAnalysisStepsAndData(plan: Plan, executionResults: Map<String, String>): String {
        return buildString {
            append("## 数据执行结果\n")

            if (executionResults.isEmpty()) {
                append("暂无执行结果数据\n")
            } else {
                val executionPlan = plan.executionPlan ?: emptyList()

                executionResults.forEach { (stepKey, stepResult) ->
                    // 过滤掉以 _analysis 结尾的 key
                    if (stepKey.endsWith("_analysis")) {
                        return@forEach
                    }

                    append("### $stepKey\n")

                    // 尝试获取对应的步骤描述
                    try {
                        val stepIndex = stepKey.replace("step_", "").toInt() - 1
                        if (stepIndex in executionPlan.indices) {
                            val step = executionPlan[stepIndex]
                            append("**步骤编号**: ${step.step}\n")
                            append("**使用工具**: ${step.toolToUse}\n")

                            step.toolParameters.let { params ->
                                append("**参数描述**: ${params.instruction}\n")
                                params.sqlQuery?.let { sql ->
                                    append("**执行SQL**: \n```sql\n$sql\n```\n")
                                }
                            }
                        }
                    } catch (e: NumberFormatException) {
                        // 忽略解析错误
                    }

                    append("**执行结果**: \n```json\n$stepResult\n```\n\n")

                    val analysisKey = "${stepKey}_analysis"
                    executionResults[analysisKey]?.takeIf { it.isNotBlank() }?.let { analysisResult ->
                        append("**Python 分析结果**: $analysisResult ")
                    }
                }
            }
        }
    }
}