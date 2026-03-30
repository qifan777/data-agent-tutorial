package io.github.qifan777.server.integration.a2a

import io.a2a.server.agentexecution.AgentExecutor
import io.a2a.server.agentexecution.RequestContext
import io.a2a.server.events.EventQueue
import org.springframework.stereotype.Component

@Component
class GraphAgentExecutor : AgentExecutor {

    override fun execute(
        context: RequestContext,
        eventQueue: EventQueue?
    ) {
    }

    override fun cancel(
        context: RequestContext?,
        eventQueue: EventQueue?
    ) {
    }

}
