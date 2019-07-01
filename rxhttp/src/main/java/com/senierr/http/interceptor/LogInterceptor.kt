package com.senierr.http.interceptor

import android.util.Log
import com.senierr.http.RxHttp
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.http.promisesBody
import okhttp3.internal.platform.Platform
import okio.Buffer
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 * 日志拦截器
 *
 * @author zhouchunjie
 * @date 2017/9/8
 */
class LogInterceptor(
        private val tag: String,
        private val logLevel: LogLevel = LogLevel.BODY,
        private val logger: Logger = DEFAULT_LOGGER
) : Interceptor {

    enum class LogLevel {
        NONE, BASIC, HEADERS, BODY
    }

    interface Logger {
        fun log(message: String)
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")
        private val DEFAULT_LOGGER = object : Logger {
            override fun log(message: String) {
                Log.println(Log.DEBUG, RxHttp::class.java.name, message)
            }
        }
    }

    private val logBasic = logLevel == LogLevel.BASIC || logLevel == LogLevel.HEADERS || logLevel == LogLevel.BODY
    private val logHeaders = logLevel == LogLevel.HEADERS || logLevel == LogLevel.BODY
    private val logBody = logLevel == LogLevel.BODY

    override fun intercept(chain: Interceptor.Chain): Response {
        val startNs = System.nanoTime()
        val response = request(chain)
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        return response(response, tookMs)
    }

    /**
     * 请求
     */
    private fun request(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (logLevel == LogLevel.NONE) {
            return chain.proceed(request)
        }

        val copyRequest = request.newBuilder().build()
        val requestBody = copyRequest.body

        val requestLog = StringBuilder()

        requestLog.append(" \n----------------------------------------\n")
        // 打印基础信息
        if (logBasic) {
            requestLog.append("\u007c-${copyRequest.method} ${copyRequest.url} ${(chain.connection()?.protocol() ?: "")}\n")
        }
        // 打印Header信息
        if (logHeaders) {
            requestLog.append("\u007C-Headers:\n")
            val headers = copyRequest.headers
            for (i in 0 until headers.size) {
                requestLog.append("\u007C\t${headers.name(i)}: ${headers.value(i)}\n")
            }
        }
        // 打印Body信息
        if (logBody && requestBody != null) {
            requestLog.append("\u007C-Body: (${requestBody.contentLength()}-byte)\n")
            if (isPlaintext(requestBody.contentType())) {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                var charset: Charset? = UTF8
                val contentType = requestBody.contentType()
                if (contentType != null) {
                    charset = contentType.charset(UTF8)
                }
                if (charset != null) {
                    requestLog.append("\u007C\t${buffer.readString(charset)}\n")
                }
            } else {
                requestLog.append("\u007C\tBody maybe [file part] , too large too print , ignored!\n")
            }
        }
        requestLog.append("----------------------------------------")

        logger.log(requestLog.toString())

        try {
            return chain.proceed(request)
        } catch (e: Exception) {
            logger.log("<-- HTTP FAILED: $e")
            throw e
        }
    }

    /**
     * 返回
     */
    private fun response(response: Response, tookMs: Long): Response {
        val responseLog = StringBuilder()

        val builder = response.newBuilder()
        val cloneResponse = builder.build()

        responseLog.append(" \n----------------------------------------\n")
        // 打印基础信息
        if (logBasic) {
            responseLog.append("\u007c-${cloneResponse.code} ${cloneResponse.message} ${cloneResponse.request.url} (${tookMs}ms)\n")
        }
        // 打印Header信息
        if (logHeaders) {
            responseLog.append("\u007C-Headers:\n")
            val headers = cloneResponse.headers
            for (i in 0 until headers.size) {
                responseLog.append("\u007C\t${headers.name(i)}: ${headers.value(i)}\n")
            }
        }
        var rawResponse = response
        // 打印Body信息
        if (logBody && cloneResponse.promisesBody()) {
            var responseBody = cloneResponse.body
            if (responseBody != null) {
                val contentLength = responseBody.contentLength()
                val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
                responseLog.append("\u007C-Body: ($bodySize)\n")
                if (isPlaintext(responseBody.contentType())) {
                    val body = responseBody.string()
                    responseLog.append("\u007C\t$body\n")
                    responseBody = body.toResponseBody(responseBody.contentType())
                    rawResponse = response.newBuilder().body(responseBody).build()
                } else {
                    responseLog.append("\u007C\tbody: maybe [file part] , too large too print , ignored!\n")
                }
            }
        }
        responseLog.append("----------------------------------------")
        logger.log(responseLog.toString())
        return rawResponse
    }

    /**
     * 判断body是否是文本内容
     */
    private fun isPlaintext(mediaType: MediaType?): Boolean {
        if (mediaType == null) return false
        if (mediaType.type == "text") {
            return true
        }
        var subtype: String? = mediaType.subtype
        if (subtype != null) {
            subtype = subtype.toLowerCase()
            return subtype.contains("x-www-form-urlencoded") ||
                    subtype.contains("json") ||
                    subtype.contains("xml") ||
                    subtype.contains("plain") ||
                    subtype.contains("html")
        }
        return false
    }
}