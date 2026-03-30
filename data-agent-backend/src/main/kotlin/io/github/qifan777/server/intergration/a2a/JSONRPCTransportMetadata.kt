package io.github.qifan777.server.intergration.a2a

import io.a2a.server.TransportMetadata
import io.a2a.spec.TransportProtocol

class JSONRPCTransportMetadata : TransportMetadata {
    override fun getTransportProtocol(): String {
        return TransportProtocol.JSONRPC.toString()
    }
}