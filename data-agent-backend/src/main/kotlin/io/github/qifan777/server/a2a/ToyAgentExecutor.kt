package io.github.qifan777.server.a2a

import com.alibaba.cloud.ai.graph.NodeOutput
import com.alibaba.cloud.ai.graph.StateGraph
import com.alibaba.cloud.ai.graph.streaming.OutputType
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput
import io.a2a.server.agentexecution.AgentExecutor
import io.a2a.server.agentexecution.RequestContext
import io.a2a.server.events.EventQueue
import io.a2a.server.tasks.TaskUpdater
import io.a2a.spec.DataPart
import io.a2a.spec.TextPart
import io.github.qifan777.server.graph.ToyGraphSpec
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class ToyAgentExecutor(
    private val stateGraph: StateGraph,
) : AgentExecutor {

    override fun execute(
        context: RequestContext,
        eventQueue: EventQueue?
    ) {
        val taskUpdater = TaskUpdater(context, eventQueue)
        val artifactNum = AtomicInteger()

        fun handleNodeOutput(nodeOutput: NodeOutput) {
            if (nodeOutput is StreamingOutput<*>) {
                when (nodeOutput.outputType) {
                    OutputType.GRAPH_NODE_STREAMING -> {
                        val message = nodeOutput.message()
                        if (message != null) {
                            taskUpdater.addArtifact(
                                listOf(TextPart(message.text)),
                                artifactNum.incrementAndGet().toString(),
                                nodeOutput.node(),
                                mapOf("outputType" to nodeOutput.outputType)
                            )
                        }
                    }

                    OutputType.GRAPH_NODE_FINISHED -> taskUpdater.addArtifact(
                        listOf(DataPart(nodeOutput.state().data())),
                        artifactNum.incrementAndGet().toString(),
                        nodeOutput.node(),
                        mapOf("outputType" to nodeOutput.outputType)
                    )

                    else -> {}
                }
                return
            }

            taskUpdater.addArtifact(
                listOf(DataPart(nodeOutput.state().data())),
                artifactNum.incrementAndGet().toString(),
                nodeOutput.node(),
                mapOf()
            )
        }

        val input = ((context.message.parts.first { it is TextPart }) as TextPart).text
        stateGraph.compile()
            .stream(mapOf(ToyGraphSpec.StateKey.INPUT to input))
            .doOnNext(::handleNodeOutput)
            .doOnComplete(taskUpdater::complete)
            .blockLast()
    }

    override fun cancel(
        context: RequestContext?,
        eventQueue: EventQueue?
    ) {
    }
}
