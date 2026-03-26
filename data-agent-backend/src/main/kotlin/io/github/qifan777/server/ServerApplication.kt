package io.github.qifan777.server

import org.babyfish.jimmer.client.EnableImplicitApi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableImplicitApi
@SpringBootApplication
open class ServerApplication

fun main(args: Array<String>) {
    runApplication<ServerApplication>(*args)
}