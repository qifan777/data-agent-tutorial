package io.github.qifan777.server.agent.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import io.github.qifan777.server.agent.DataAgentSpec
import io.github.qifan777.server.graph.nodes.SqlExecuteNode
import io.github.qifan777.server.shared.json.JsonUtil
import io.github.qifan777.server.shared.python.SimplePythonExecutor
import org.springframework.stereotype.Component

@Component
class PythonExecuteNode : NodeAction {
    override fun apply(state: OverAllState): Map<String, Any> {
        val pythonCode = state.value(DataAgentSpec.Graph.StateKey.Execution.PYTHON_GENERATION_RESULT, "")
        val result =
            state.value(DataAgentSpec.Graph.StateKey.Execution.SQL_EXECUTION_RESULT, SqlExecuteNode.Result::class.java).orElseThrow()
        val inputDataJson = JsonUtil.toJson(result.resultSet.data) ?: throw RuntimeException("sql结果为空")
        val output = SimplePythonExecutor.execute(pythonCode, inputDataJson)
        return mapOf(DataAgentSpec.Graph.StateKey.Execution.PYTHON_EXECUTION_RESULT to output)
    }

}