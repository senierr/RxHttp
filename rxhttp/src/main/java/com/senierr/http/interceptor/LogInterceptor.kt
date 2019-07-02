package com.senierr.http.interceptor

import android.util.Log
import com.senierr.http.RxHttp
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.http.promisesBody
import okio.Buffer
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * 日志拦截器
 *
 * @author zhouchunjie
 * @date 2017/9/8
 */
class LogInterceptor(
        private val tag: String = RxHttp::class.java.simpleName,
        private val logLevel: LogLevel = LogLevel.BODY,
        private val logger: Logger = DEFAULT_LOGGER
) : Interceptor {

    enum class LogLevel {
        NONE, BASIC, HEADERS, BODY
    }

    interface Logger {
        fun log(tag: String, message: String)
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")
        val DEFAULT_LOGGER = object : Logger {
            override fun log(tag: String, message: String) {
                Log.println(Log.DEBUG, tag, message)
            }
        }
    }

    private val logBasic = logLevel == LogLevel.BASIC || logLevel == LogLevel.HEADERS || logLevel == LogLevel.BODY
    private val logHeaders = logLevel == LogLevel.HEADERS || logLevel == LogLevel.BODY
    private val logBody = logLevel == LogLevel.BODY

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (logLevel == LogLevel.NONE) {
            return chain.proceed(request)
        }

        val copyRequest = request.newBuilder().build()
        val requestBody = copyRequest.body

        val requestLog = StringBuilder()
        requestLog.append(" \n┌─────────────────────────────────────────────────────────────────────────────────────\n")
        // 打印基础信息
        if (logBasic) {
            requestLog.append("├ ${copyRequest.method} ${copyRequest.url} ${(chain.connection()?.protocol() ?: "")}\n")
        }
        // 打印Header信息
        if (logHeaders) {
            requestLog.append("├ Headers:\n")
            val headers = copyRequest.headers
            for (i in 0 until headers.size) {
                requestLog.append("│\t${headers.name(i)}: ${headers.value(i)}\n")
            }
        }
        // 打印Body信息
        if (logBody && requestBody != null) {
            requestLog.append("├ Body: (${requestBody.contentLength()}-byte)\n")
            if (isPlaintext(requestBody.contentType())) {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                var charset: Charset? = UTF8
                val contentType = requestBody.contentType()
                if (contentType != null) {
                    charset = contentType.charset(UTF8)
                }
                if (charset != null) {
                    requestLog.append("│\t${buffer.readString(charset)}\n")
                }
            } else {
                requestLog.append("│\tBody maybe [file part] , too large too print , ignored!\n")
            }
        }
        requestLog.append("└─────────────────────────────────────────────────────────────────────────────────────")
        logger.log(tag, requestLog.toString())

        val responseLog = StringBuilder()
        responseLog.append(" \n┌─────────────────────────────────────────────────────────────────────────────────────\n")
        // 开始请求
        val startNs = System.nanoTime()
        var response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            responseLog.append("├ ${copyRequest.method} ${copyRequest.url} ${(chain.connection()?.protocol() ?: "")}\n")
            responseLog.append("├ FAILED: $e\n")
            responseLog.append("└─────────────────────────────────────────────────────────────────────────────────────")
            logger.log(tag, responseLog.toString())
            throw e
        }
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        // 结束请求

        val builder = response.newBuilder()
        val cloneResponse = builder.build()

        // 打印基础信息
        if (logBasic) {
            responseLog.append("├ ${cloneResponse.code} ${cloneResponse.message} ${cloneResponse.request.url} (${tookMs}ms)\n")
        }
        // 打印Header信息
        if (logHeaders) {
            responseLog.append("├ Headers:\n")
            val headers = cloneResponse.headers
            for (i in 0 until headers.size) {
                responseLog.append("│\t${headers.name(i)}: ${headers.value(i)}\n")
            }
        }
        // 打印Body信息
        if (logBody && cloneResponse.promisesBody()) {
            var responseBody = cloneResponse.body
            if (responseBody != null) {
                val contentLength = responseBody.contentLength()
                val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
                responseLog.append("├ Body: ($bodySize)\n")
                if (isPlaintext(responseBody.contentType())) {
                    val body = responseBody.string()
                    responseLog.append("│\t$body\n")
                    responseBody = body.toResponseBody(responseBody.contentType())
                    response = response.newBuilder().body(responseBody).build()
                } else {
                    responseLog.append("│\tbody: maybe [file part] , too large too print , ignored!\n")
                }
            }
        }
        responseLog.append("└─────────────────────────────────────────────────────────────────────────────────────")
        logger.log(tag, responseLog.toString())

        return response
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