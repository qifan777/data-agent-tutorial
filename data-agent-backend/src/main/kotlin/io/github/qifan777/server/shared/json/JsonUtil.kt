package io.github.qifan777.server.shared.json

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class JsonUtil {

    // 静态属性，对外公开
    companion object {
        lateinit var objectMapper: ObjectMapper
            private set

        /**
         * 对象转 JSON 字符串
         */
        @JvmStatic
        fun toJson(obj: Any?): String? {
            return obj?.let {
                try {
                    objectMapper.writeValueAsString(it)
                } catch (e: Exception) {
                    throw RuntimeException("JSON serialization error", e)
                }
            }
        }

        /**
         * JSON 字符串转对象 (Kotlin 友好版)
         */
        inline fun <reified T> fromJson(json: String?): T? {
            if (json.isNullOrBlank()) return null
            return try {
                objectMapper.readValue<T>(json)
            } catch (e: Exception) {
                throw RuntimeException("JSON deserialization error", e)
            }
        }

        /**
         * 兼容 Java 的调用方法
         */
        @JvmStatic
        fun <T> fromJson(json: String?, clazz: Class<T>): T? {
            if (json.isNullOrBlank()) return null
            return try {
                objectMapper.readValue(json, clazz)
            } catch (e: Exception) {
                throw RuntimeException("JSON deserialization error", e)
            }
        }
    }

    // 通过 Spring 注入实例并赋值给静态变量
    @Autowired
    private lateinit var springObjectMapper: ObjectMapper

    @PostConstruct
    fun init() {
        objectMapper = springObjectMapper
    }
}