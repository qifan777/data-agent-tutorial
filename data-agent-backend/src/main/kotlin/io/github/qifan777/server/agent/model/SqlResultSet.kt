package io.github.qifan777.server.agent.model

data class SqlResultSet(
    var column: List<String> = mutableListOf(),
    var data: List<Map<String, String>> = mutableListOf(),
    var errorMsg: String? = null
) : Cloneable {

    public override fun clone(): SqlResultSet {
        return SqlResultSet(
            column = ArrayList(this.column),
            // 深度复制 Map 列表
            data = this.data.map { HashMap(it) }
        )
    }
}
