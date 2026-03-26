package io.github.qifan777.server.a2a


import io.a2a.server.agentexecution.AgentExecutor
import io.a2a.server.events.InMemoryQueueManager
import io.a2a.server.events.QueueManager
import io.a2a.server.requesthandlers.DefaultRequestHandler
import io.a2a.server.tasks.*
import io.a2a.spec.AgentCapabilities
import io.a2a.spec.AgentCard
import io.a2a.spec.AgentInterface
import io.a2a.spec.AgentSkill
import io.a2a.transport.jsonrpc.handler.JSONRPCHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors

@Configuration
open class A2AConfiguration {
    @Bean
    open fun agentCard(): AgentCard {
        return AgentCard.Builder()
            .name("sqlAgent")
            .description("专业的SQL生成Agent")
            .defaultInputModes(listOf("text/plain"))
            .defaultOutputModes(listOf("text/plain"))
            .capabilities(
                AgentCapabilities.Builder()
                    .streaming(true)
                    .pushNotifications(true)
                    .stateTransitionHistory(true)
                    .build()
            )
            .skills(
                listOf(
                    AgentSkill.Builder()
                        .id("sql")
                        .name("sql generator")
                        .description("Generate a SQL query")
                        .tags(listOf("sql", "query"))
                        .build()
                )
            )
            .url("http://localhost:3500/api/a2a/jsonrpc")
            .additionalInterfaces(listOf(AgentInterface("JSONRPC", "http://localhost:3500/api/a2a/jsonrpc")))
            .version("1.0")
            .build()
    }


    @Bean
    open fun taskStore(): InMemoryTaskStore {
        return InMemoryTaskStore()
    }

    @Bean
    open fun pushNotificationConfigStore(): InMemoryPushNotificationConfigStore {
        return InMemoryPushNotificationConfigStore()
    }

    @Bean
    open fun pushNotificationSender(pushNotificationConfigStore: PushNotificationConfigStore): BasePushNotificationSender {
        return BasePushNotificationSender(pushNotificationConfigStore)
    }


    @Bean
    open fun queueManager(taskStore: InMemoryTaskStore): QueueManager {
        return InMemoryQueueManager(taskStore)
    }


    @Bean
    open fun agentCompletionTimeoutSeconds(
        @Value("\${a2a.blocking.agent.timeout.seconds:30}") timeout: Int
    ): Int {
        return timeout
    }

    @Bean
    open fun jsonRpcHandler(
        agentCard: AgentCard,
        agentExecutor: AgentExecutor,
        taskStore: TaskStore,
        queueManager: QueueManager,
        pushNotificationConfigStore: PushNotificationConfigStore,
        pushNotificationSender: PushNotificationSender
    ): JSONRPCHandler {
        val pool = Executors.newFixedThreadPool(5);
        val handler = DefaultRequestHandler.create(
            agentExecutor,
            taskStore,
            queueManager,
            pushNotificationConfigStore,
            pushNotificationSender,
            pool
        )
        return JSONRPCHandler(agentCard, handler, pool)
    }

}
