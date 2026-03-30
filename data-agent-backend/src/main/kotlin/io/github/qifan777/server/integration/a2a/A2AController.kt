package io.github.qifan777.server.integration.a2a

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.io.JsonEOFException
import io.a2a.common.A2AHeaders
import io.a2a.server.ServerCallContext
import io.a2a.server.auth.UnauthenticatedUser
import io.a2a.server.extensions.A2AExtensions
import io.a2a.spec.*
import io.a2a.transport.jsonrpc.handler.JSONRPCHandler
import io.a2a.util.Utils
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.babyfish.jimmer.client.ApiIgnore
import org.babyfish.jimmer.jackson.v2.ImmutableModuleV2
import org.reactivestreams.FlowAdapters
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import reactor.core.publisher.Flux
import java.util.concurrent.Flow

private val log = KotlinLogging.logger {}

@RestController
class A2AController(
    private val request: HttpServletRequest,
    private val jsonRpcHandler: JSONRPCHandler,
) {
    companion object {
        private const val METHOD_NAME_KEY = "methodName"
        private const val HEADERS_KEY = "headers"
    }

    @PostConstruct
    fun init() {
        Utils.OBJECT_MAPPER.registerModule(ImmutableModuleV2())
    }

    @GetMapping(value = ["/.well-known/agent-card.json"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun agentJson(): AgentCard {
        return jsonRpcHandler.agentCard
    }

    @ApiIgnore
    @PostMapping(
        value = ["/a2a/jsonrpc"],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE]
    )
    fun handleRequest(@RequestBody body: String): Any {
        var streaming = false
        val context = createCallContext()
        var nonStreamingResponse: JSONRPCResponse<*>? = null
        var streamingResponse: Flux<out JSONRPCResponse<*>>? = null
        var error: JSONRPCErrorResponse? = null

        try {
            val node = Utils.OBJECT_MAPPER.readTree(body)
            val method = node?.get("method")
            streaming = method != null && (
                    SendStreamingMessageRequest.METHOD == method.asText() ||
                            TaskResubscriptionRequest.METHOD == method.asText()
                    )
            val methodName = if (method != null && method.isTextual) method.asText() else null
            if (methodName != null) {
                context.state[METHOD_NAME_KEY] = methodName
            }

            if (streaming) {
                val request = Utils.OBJECT_MAPPER.treeToValue(node, StreamingJSONRPCRequest::class.java)
                streamingResponse = processStreamingRequest(request, context)
            } else {
                val request = Utils.OBJECT_MAPPER.treeToValue(node, NonStreamingJSONRPCRequest::class.java)
                nonStreamingResponse = processNonStreamingRequest(request, context)
            }
        } catch (e: JsonProcessingException) {
            error = handleError(e)
        } catch (t: Throwable) {
            error = JSONRPCErrorResponse(InternalError(t.message))
        }

        if (error != null) {
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Utils.toJsonString(error))
        }

        if (streaming) {
            val emitter = SseEmitter(0L)
            val flux = streamingResponse ?: Flux.just(JSONRPCErrorResponse(InternalError("Streaming response is null")))
            flux.subscribe(
                { response ->
                    try {
                        emitter.send(SseEmitter.event().data(Utils.toJsonString(response)))
                    } catch (e: Exception) {
                        emitter.completeWithError(e)
                    }
                },
                { e ->
                    log.error(e) { "SSE transport failed" }
                    emitter.completeWithError(e)
                },
                { emitter.complete() }
            )
            return emitter
        }

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(Utils.toJsonString(nonStreamingResponse))
    }

    private fun handleError(exception: JsonProcessingException): JSONRPCErrorResponse {
        var id: Any? = null
        val jsonRpcError: JSONRPCError = when {
            exception.cause is JsonParseException -> JSONParseError()
            exception is JsonEOFException -> JSONParseError(exception.message)
            exception is MethodNotFoundJsonMappingException -> {
                id = exception.id
                MethodNotFoundError()
            }

            exception is InvalidParamsJsonMappingException -> {
                id = exception.id
                InvalidParamsError()
            }

            exception is IdJsonMappingException -> {
                id = exception.id
                InvalidRequestError()
            }

            else -> InvalidRequestError()
        }
        return JSONRPCErrorResponse(id, jsonRpcError)
    }

    private fun processNonStreamingRequest(
        request: NonStreamingJSONRPCRequest<*>,
        context: ServerCallContext
    ): JSONRPCResponse<*> {
        return when (request) {
            is GetTaskRequest -> jsonRpcHandler.onGetTask(request, context)
            is CancelTaskRequest -> jsonRpcHandler.onCancelTask(request, context)
            is SetTaskPushNotificationConfigRequest -> jsonRpcHandler.setPushNotificationConfig(request, context)
            is GetTaskPushNotificationConfigRequest -> jsonRpcHandler.getPushNotificationConfig(request, context)
            is SendMessageRequest -> jsonRpcHandler.onMessageSend(request, context)
            is ListTaskPushNotificationConfigRequest -> jsonRpcHandler.listPushNotificationConfig(request, context)
            is DeleteTaskPushNotificationConfigRequest -> jsonRpcHandler.deletePushNotificationConfig(request, context)
            is GetAuthenticatedExtendedCardRequest -> jsonRpcHandler.onGetAuthenticatedExtendedCardRequest(
                request,
                context
            )

            else -> generateErrorResponse(request, UnsupportedOperationError())
        }
    }

    private fun processStreamingRequest(
        request: JSONRPCRequest<*>,
        context: ServerCallContext
    ): Flux<out JSONRPCResponse<*>> {
        val publisher: Flow.Publisher<out JSONRPCResponse<*>> = when (request) {
            is SendStreamingMessageRequest -> jsonRpcHandler.onMessageSendStream(request, context)
            is TaskResubscriptionRequest -> jsonRpcHandler.onResubscribeToTask(request, context)
            else -> return Flux.just(generateErrorResponse(request, UnsupportedOperationError()))
        }
        return Flux.from(FlowAdapters.toPublisher(publisher))
    }

    private fun generateErrorResponse(request: JSONRPCRequest<*>, error: JSONRPCError): JSONRPCResponse<*> {
        return JSONRPCErrorResponse(request.id, error)
    }

    private fun createCallContext(): ServerCallContext {
        val user = UnauthenticatedUser.INSTANCE
        val state = mutableMapOf<String, Any>()
        val requestHeaders = mutableMapOf<String, String>()

        request.headerNames.asIterator().forEach { name ->
            requestHeaders[name] = request.getHeader(name)
        }
        state[HEADERS_KEY] = requestHeaders

        val extensionHeaderValues = mutableListOf<String>()
        request.getHeaders(A2AHeaders.X_A2A_EXTENSIONS).asIterator().forEach {
            extensionHeaderValues.add(it)
        }
        val requestedExtensions = A2AExtensions.getRequestedExtensions(extensionHeaderValues)

        return ServerCallContext(user, state, requestedExtensions)
    }
}