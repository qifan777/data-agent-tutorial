package io.github.qifan777.server.agent.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription


data class EvidenceQueryRewriteResult(
    @get:JsonPropertyDescription("重写后的完整句子")
    @get:JsonProperty("standalone_query")
    var standaloneQuery: String = ""
)
