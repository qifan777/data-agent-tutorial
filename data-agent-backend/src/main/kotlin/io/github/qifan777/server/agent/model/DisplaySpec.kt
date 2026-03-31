package io.github.qifan777.server.agent.model

import com.fasterxml.jackson.annotation.JsonPropertyDescription

data class DisplaySpec(
    /**
     * 图表类型，如：table, bar, line, pie等
     */
    @get:JsonPropertyDescription("图表类型，取值范围：table、 bar、 line、 pie等")
    var type: String? = null,

    /**
     * 图表标题
     */
    @get:JsonPropertyDescription("图表标题")
    var title: String? = null,

    /**
     * X轴字段名
     */
    @get:JsonPropertyDescription("X轴字段名")
    var x: String? = null,

    /**
     * Y轴字段名列表
     */
    @get:JsonPropertyDescription("Y轴字段名列表")
    var y: List<String>? = null
)
