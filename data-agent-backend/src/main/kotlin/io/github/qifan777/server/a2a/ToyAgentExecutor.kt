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
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class ToyAgentExecutor(private val stateGraph: StateGraph) : AgentExecutor {
    override fun execute(
        context: RequestContext?,
        eventQueue: EventQueue?
    ) {
        val taskUpdater = TaskUpdater(context, eventQueue)
        val artifactNum = AtomicInteger()
        fun handleNodeOutput(nodeOutput: NodeOutput) {
            if (nodeOutput is StreamingOutput<*>) {
                when (nodeOutput.outputType) {
                    OutputType.GRAPH_NODE_STREAMING -> taskUpdater.addArtifact(
                        listOf(TextPart(nodeOutput.message().text)),
                        artifactNum.incrementAndGet().toString(),
                        nodeOutput.node(),
                        mapOf(
                            "outputType" to nodeOutput.outputType
                        )
                    )

                    OutputType.GRAPH_NODE_FINISHED -> taskUpdater.addArtifact(
                        listOf(DataPart(nodeOutput.state().data())),
                        artifactNum.incrementAndGet().toString(),
                        nodeOutput.node(),
                        mapOf(
                            "outputType" to nodeOutput.outputType
                        )
                    )

                    else -> {}
                }
            } else {
                taskUpdater.addArtifact(
                    listOf(DataPart(nodeOutput.state().data())),
                    artifactNum.incrementAndGet().toString(),
                    nodeOutput.node(),
                    mapOf()
                )
            }

        }

        val text = ((context?.message?.parts?.first { it is TextPart }) as TextPart).text
        stateGraph.compile().stream(mapOf("input" to text))
            .doOnNext(::handleNodeOutput)
            .doOnComplete(taskUpdater::complete)
            .blockLast()
    }

    //    override fun execute(
//        context: RequestContext?,
//        eventQueue: EventQueue?
//    ) {
//        val taskUpdater = TaskUpdater(context, eventQueue)
//        val text = ((context?.message?.parts?.first { it is TextPart }) as TextPart).text
//        taskUpdater.addArtifact(
//            listOf(TextPart("hello from a2a: $text")),
//            "1",
//            "TOY_HELLO_NODE",
//            mapOf("outputType" to "GRAPH_NODE_STREAMING")
//        )
//        taskUpdater.complete()
//    }


    override fun cancel(
        context: RequestContext?,
        eventQueue: EventQueue?
    ) {
    }
}