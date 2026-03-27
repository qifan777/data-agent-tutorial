package io.github.qifan777.server.graph

import com.alibaba.cloud.ai.graph.StateGraph
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction
import io.github.qifan777.server.graph.nodes.ToyHelloNode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class GraphConfiguration {
    @Bean
    open fun toyHelloGraph(toyHelloNode: ToyHelloNode): StateGraph {
        return StateGraph()
            .addNode("TOY_HELLO_NODE", AsyncNodeAction.node_async(toyHelloNode))
            .addEdge(StateGraph.START, "TOY_HELLO_NODE")
            .addEdge("TOY_HELLO_NODE", StateGraph.END)
    }
}
