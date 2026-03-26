package io.github.qifan777.server.hello

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController {
    data class Message(val message: String)

    @GetMapping("/hello")
    fun hello(): Message {
        return Message("QiFan")
    }
}