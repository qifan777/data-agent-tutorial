package io.github.qifan777.server.a2a

import io.a2a.server.agentexecution.AgentExecutor
import io.a2a.server.agentexecution.RequestContext
import io.a2a.server.events.EventQueue
import io.a2a.server.tasks.TaskUpdater
import io.a2a.spec.TextPart
import org.springframework.stereotype.Component

@Component
class ToyAgentExecutor : AgentExecutor {
    override fun execute(
        context: RequestContext?,
        eventQueue: EventQueue?
    ) {
        val taskUpdater = TaskUpdater(context, eventQueue)
        val text = ((context?.message?.parts?.first { it is TextPart }) as TextPart).text
        taskUpdater.addArtifact(
            listOf(TextPart("hello from a2a: $text")),
            "1",
            "TOY_HELLO_NODE",
            mapOf("outputType" to "GRAPH_NODE_STREAMING")
        )
        taskUpdater.complete()
    }

    override fun cancel(
        context: RequestContext?,
        eventQueue: EventQueue?
    ) {
    }
}