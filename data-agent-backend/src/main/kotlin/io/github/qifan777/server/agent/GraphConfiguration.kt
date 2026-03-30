package io.github.qifan777.server.agent

import com.alibaba.cloud.ai.graph.StateGraph
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class GraphConfiguration {
    @Bean
    open fun graph(): StateGraph {
        return StateGraph { mapOf() }
    }
}
