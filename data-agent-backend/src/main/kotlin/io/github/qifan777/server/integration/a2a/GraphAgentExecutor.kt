package io.github.qifan777.server.integration.a2a

import com.alibaba.cloud.ai.graph.*
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver
import com.alibaba.cloud.ai.graph.streaming.OutputType
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput
import io.a2a.server.agentexecution.AgentExecutor
import io.a2a.server.agentexecution.RequestContext
import io.a2a.server.events.EventQueue
import io.a2a.server.tasks.TaskStore
import io.a2a.server.tasks.TaskUpdater
import io.a2a.spec.*
import io.github.qifan777.server.agent.DataAgentSpec
import io.github.qifan777.server.agent.DataAgentSpec.Graph.Node.INTERRUPT_NODE
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@Component
class GraphAgentExecutor(
    private val stateGraph: StateGraph,
    private val taskStore: TaskStore,
) : AgentExecutor {
    private val saver = MemorySaver()
    override fun execute(
        context: RequestContext,
        eventQueue: EventQueue?
    ) {
        val taskUpdater = TaskUpdater(context, eventQueue)
        val artifactNum = AtomicInteger()
        val compiledGraph = stateGraph.compile(
            CompileConfig.builder()
                .saverConfig(SaverConfig.builder().register(saver).build())
                .interruptBefore(INTERRUPT_NODE)
                .build()
        )

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

        val message = context.message
        val existingTask = message.taskId?.let(taskStore::get)
        if (existingTask != null) {
            val runnableConfig = RunnableConfig.builder().threadId(existingTask.id).build()
            val approved =
                message.metadata[DataAgentSpec.MessageMetadataKey.CONFIRMATION_APPROVED]
            val feedback =
                message.metadata[DataAgentSpec.MessageMetadataKey.CONFIRMATION_FEEDBACK]
            val resumedConfig = compiledGraph.updateState(
                runnableConfig,
                mapOf(
                    DataAgentSpec.Graph.StateKey.HumanReview.CONFIRMATION_APPROVED to approved,
                    DataAgentSpec.Graph.StateKey.HumanReview.CONFIRMATION_FEEDBACK to feedback,
                )
            )
            compiledGraph.stream(null, resumedConfig)
                .doOnNext(::handleNodeOutput)
                .doOnComplete {
                    onComplete(compiledGraph, taskUpdater, runnableConfig)
                }
                .blockLast()
            return
        }

        val newTask = newTask(message)
        eventQueue?.enqueueEvent(newTask)

        val input = ((message.parts.first { it is TextPart }) as TextPart).text
        val runnableConfig = RunnableConfig.builder().threadId(newTask.id).build()

        compiledGraph.stream(
            mapOf(
                DataAgentSpec.Graph.StateKey.Input.USER_INPUT to input,
                DataAgentSpec.Graph.StateKey.Input.DATABASE_ID to message.metadata[DataAgentSpec.MessageMetadataKey.DATABASE_ID]
            ), runnableConfig
        )
            .doOnNext(::handleNodeOutput)
            .doOnComplete {
                onComplete(compiledGraph, taskUpdater, runnableConfig)
            }
            .blockLast()
    }

    override fun cancel(
        context: RequestContext?,
        eventQueue: EventQueue?
    ) {
    }

    private fun newTask(request: Message): Task {
        var contextId = request.contextId
        if (contextId == null || contextId.isEmpty()) {
            contextId = UUID.randomUUID().toString()
        }
        var id = UUID.randomUUID().toString()
        if (request.taskId != null && !request.taskId.isEmpty()) {
            id = request.taskId
        }
        return Task(id, contextId, TaskStatus(TaskState.SUBMITTED), null, listOf(request), null)
    }

    private fun onComplete(compiledGraph: CompiledGraph, taskUpdater: TaskUpdater, runnableConfig: RunnableConfig) {
        val stateSnapshot = compiledGraph.getState(runnableConfig)
        if (stateSnapshot.next() == INTERRUPT_NODE) {
            taskUpdater.requiresInput()
        }
    }

}

