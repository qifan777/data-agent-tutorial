package io.github.qifan777.server.shared.python

import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit

// 封装执行结果
data class PythonExecutionResult(
    val success: Boolean,
    val output: String,
    val error: String
)

object SimplePythonExecutor {
    private const val DEFAULT_DOCKER_IMAGE = "continuumio/anaconda3:latest"
    private const val DEFAULT_MEMORY_LIMIT = "512m"

    /**
     * 执行 Python 代码
     *
     * @param pythonCode 完整的 Python 代码字符串
     * @param inputJson  需要通过 stdin 传入的 JSON 数据字符串
     * @param timeoutSec 超时时间(秒)
     */
    fun execute(pythonCode: String, inputJson: String, timeoutSec: Long = 60): PythonExecutionResult {
        val workDir = Files.createTempDirectory("ai_python_exec_").toFile()
        val scriptFile = File(workDir, "script.py")
        val dataFile = File(workDir, "input.json")

        return try {
            // 1. 写入脚本和输入数据
            scriptFile.writeText(pythonCode)
            dataFile.writeText(inputJson)

            // 2. 强制使用 Docker 沙箱执行，保证依赖与隔离一致
            if (!isDockerAvailable()) {
                return PythonExecutionResult(
                    success = false,
                    output = "",
                    error = "Docker is required for Python sandbox execution but was not found. Please install/start Docker."
                )
            }

            val dockerImage = System.getenv("DATA_AGENT_PYTHON_DOCKER_IMAGE")?.trim().orEmpty()
            val image = if (dockerImage.isBlank()) DEFAULT_DOCKER_IMAGE else dockerImage
            val memoryLimit = System.getenv("DATA_AGENT_PYTHON_MEMORY_LIMIT")?.trim().orEmpty().ifBlank { DEFAULT_MEMORY_LIMIT }

            val dockerResult = runCommand(
                listOf(
                    "docker", "run", "--rm", "-i",
                    "--network", "none",
                    "--cpus", "1",
                    "--memory", memoryLimit,
                    "--pids-limit", "128",
                    "--security-opt", "no-new-privileges",
                    "-v", "${workDir.absolutePath}:/work:ro",
                    "-w", "/work",
                    image,
                    "python",
                    "/work/script.py",
                ),
                dataFile,
                timeoutSec
            )

            if (dockerResult.success) {
                dockerResult
            } else {
                dockerResult.copy(error = "${dockerResult.error}\n\nSandbox image: $image")
            }

        } catch (e: Exception) {
            PythonExecutionResult(false, "", e.message ?: "Unknown error occurred")
        } finally {
            // 3. 清理临时目录
            workDir.deleteRecursively()
        }
    }

    private fun runCommand(command: List<String>, inputFile: File, timeoutSec: Long): PythonExecutionResult {
        val process = ProcessBuilder(command)
            .redirectInput(inputFile)
            .start()

        val completed = process.waitFor(timeoutSec, TimeUnit.SECONDS)
        if (!completed) {
            process.destroyForcibly()
            return PythonExecutionResult(false, "", "Execution timed out after $timeoutSec seconds.")
        }

        val exitCode = process.exitValue()
        val stdout = process.inputStream.bufferedReader().readText()
        val stderr = process.errorStream.bufferedReader().readText()
        return if (exitCode == 0) {
            PythonExecutionResult(true, stdout.trim(), stderr.trim())
        } else {
            PythonExecutionResult(false, stdout.trim(), stderr.trim())
        }
    }

    private fun isDockerAvailable(): Boolean {
        return try {
            val process = ProcessBuilder("docker", "--version").start()
            process.waitFor(3, TimeUnit.SECONDS) && process.exitValue() == 0
        } catch (_: Exception) {
            false
        }
    }
}
