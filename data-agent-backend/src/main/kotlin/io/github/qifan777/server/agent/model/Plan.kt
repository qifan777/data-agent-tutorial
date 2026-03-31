package io.github.qifan777.server.agent.model

import com.alibaba.cloud.ai.graph.OverAllState
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import io.github.qifan777.server.agent.DataAgentSpec
import io.github.qifan777.server.shared.json.JsonUtil

data class Plan(
    @get:JsonProperty("thought_process")
    @get:JsonPropertyDescription("简要描述你的分析思路。必须明确提到你检查了哪些表和字段")
    var thoughtProcess: String,

    @get:JsonProperty("execution_plan")
    @get:JsonPropertyDescription("执行计划的步骤列表")
    var executionPlan: List<PlanStep>
) {

    companion object {
        fun getPlan(state: OverAllState): Plan {
            val planStr = state.value(DataAgentSpec.Graph.StateKey.Planning.PLAN, "")
            return JsonUtil.fromJson(planStr, Plan::class.java) ?: throw RuntimeException("plan json反序列化失败")
        }

        fun getCurrentStep(state: OverAllState): PlanStep {
            val plan = getPlan(state)
            val step = state.value(DataAgentSpec.Graph.StateKey.Planning.CURRENT_STEP, 1)
            return plan.executionPlan[step - 1]
        }
    }
}

data class PlanStep(
    @get:JsonProperty("step")
    @get:JsonPropertyDescription("步骤顺序号")
    var step: Int = 0,

    @get:JsonProperty("tool_to_use")
    @get:JsonPropertyDescription("工具名称")
    var toolToUse: String,

    @get:JsonProperty("tool_parameters")
    @get:JsonPropertyDescription("工具参数")
    var toolParameters: ToolParameters
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class ToolParameters(
        // 1. DataAgentSpec.Graph.Node.SQL_GENERATION -> 存当前步骤要做的详细的 SQL 需求
        // 2. DataAgentSpec.Graph.Node.PYTHON_GENERATION -> 存当前步骤要做的详细的编程需求
        @get:JsonProperty("instruction")
        @get:JsonPropertyDescription("当工具名称tool_to_use是DataAgentSpec.Graph.Node.SQL_GENERATION时这里的值为 当前步骤要做的详细的 SQL 需求，是DataAgentSpec.Graph.Node.PYTHON_GENERATION时填当前步骤要做的详细的编程需求")
        var instruction: String? = null,

        // DataAgentSpec.Graph.Node.REPORT_GENERATION专用。报告的大纲、需要回答的关键问题和建议方向。
        @get:JsonProperty("summary_and_recommendations")
        @get:JsonPropertyDescription("DataAgentSpec.Graph.Node.REPORT_GENERATION节点专用,仅 REPORT 节点需要此字段，报告的大纲")
        var summaryAndRecommendations: String? = null,

        // --- 运行态字段 ---
        @get:JsonProperty("sql_query")
        @get:JsonPropertyDescription("DataAgentSpec.Graph.Node.SQL_GENERATION 运行完后，会把生成的 SQL 塞进来")
        var sqlQuery: String? = null
    )
}
