package io.github.qifan777.server.a2a

import com.alibaba.cloud.ai.graph.CompileConfig
import com.alibaba.cloud.ai.graph.NodeOutput
import com.alibaba.cloud.ai.graph.RunnableConfig
import com.alibaba.cloud.ai.graph.StateGraph
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
import io.github.qifan777.server.graph.ToyGraphSpec
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@Component
class ToyAgentExecutor(
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
                .interruptBefore(ToyGraphSpec.Node.CONFIRM)
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
                message.metadata[ToyGraphSpec.MessageMetadataKey.CONFIRMATION_APPROVED]
            val feedback =
                message.metadata[ToyGraphSpec.MessageMetadataKey.CONFIRMATION_FEEDBACK]
            val resumedConfig = compiledGraph.updateState(
                runnableConfig,
                mapOf(
                    ToyGraphSpec.StateKey.CONFIRMATION_APPROVED to approved,
                    ToyGraphSpec.StateKey.CONFIRMATION_FEEDBACK to feedback,
                )
            )
            compiledGraph.stream(null, resumedConfig)
                .doOnNext(::handleNodeOutput)
                .doOnComplete(taskUpdater::complete)
                .blockLast()
            return
        }

        val newTask = newTask(message)
        eventQueue?.enqueueEvent(newTask)

        val input = ((message.parts.first { it is TextPart }) as TextPart).text
        val runnableConfig = RunnableConfig.builder().threadId(newTask.id).build()

        compiledGraph.stream(mapOf(ToyGraphSpec.StateKey.INPUT to input), runnableConfig)
            .doOnNext(::handleNodeOutput)
            .doOnComplete {
                val stateSnapshot = compiledGraph.getState(runnableConfig)
                val stateData = LinkedHashMap(stateSnapshot.state().data())
                taskUpdater.addArtifact(
                    listOf(
                        DataPart(
                            mapOf(
                                ToyGraphSpec.StateKey.SCENE to stateData[ToyGraphSpec.StateKey.SCENE],
                                ToyGraphSpec.StateKey.SCENE_LABEL to stateData[ToyGraphSpec.StateKey.SCENE_LABEL],
                                ToyGraphSpec.ArtifactDataKey.NEED_CONFIRMATION to true,
                            )
                        )
                    ),
                    artifactNum.incrementAndGet().toString(),
                    ToyGraphSpec.Node.CONFIRM,
                    mapOf("outputType" to ToyGraphSpec.ArtifactOutputType.HUMAN_CONFIRMATION)
                )
                taskUpdater.requiresInput(
                    taskUpdater.newAgentMessage(
                        listOf(TextPart("已判断当前场景为${stateData[ToyGraphSpec.StateKey.SCENE_LABEL]}，请确认是否继续执行。")),
                        mapOf(
                            ToyGraphSpec.StateKey.SCENE to stateData[ToyGraphSpec.StateKey.SCENE],
                            ToyGraphSpec.StateKey.SCENE_LABEL to stateData[ToyGraphSpec.StateKey.SCENE_LABEL],
                        )
                    )
                )
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
}
