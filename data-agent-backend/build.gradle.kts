plugins {
    kotlin("jvm") version "2.3.0"
    id("com.google.devtools.ksp") version "2.3.0"
    id("org.springframework.boot") version "3.5.12"
    id("tech.argonariod.gradle-plugin-jimmer") version "latest.release"
}
apply(plugin = "io.spring.dependency-management")

group = "io.github.qifan777"
version = "1.0"
jimmer {
    version = "0.10.6"
}


dependencies {
    implementation(platform("org.springframework.ai:spring-ai-bom:1.1.2"))
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    implementation("com.alibaba.cloud.ai:spring-ai-alibaba-graph-core:1.1.2.2")
    implementation("io.github.a2asdk:a2a-java-sdk-transport-jsonrpc:0.3.2.Final")
    implementation("io.projectreactor.netty:reactor-netty")
    implementation("io.github.oshai:kotlin-logging-jvm:8.0.01")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.postgresql:postgresql")
}
kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}