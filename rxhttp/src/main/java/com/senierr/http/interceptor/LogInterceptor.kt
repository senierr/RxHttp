package com.senierr.http.interceptor

import android.util.Log
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
        private val tag: String,
        private val logLevel: LogLevel
) : Interceptor {

    enum class LogLevel {
        NONE, BASIC, HEADERS, BODY
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (logLevel == LogLevel.NONE) {
            return chain.proceed(request)
        }

        val logBasic = (logLevel == LogLevel.BASIC
                || logLevel == LogLevel.HEADERS
                || logLevel == LogLevel.BODY)
        val logHeaders = logLevel == LogLevel.HEADERS || logLevel == LogLevel.BODY
        val logBody = logLevel == LogLevel.BODY

        val requestLog = StringBuilder()

        val copyRequest = request.newBuilder().build()
        val requestBody = copyRequest.body

        // 打印基础信息
        if (logBasic) {
            requestLog.append(" \n--> ${copyRequest.method} ${copyRequest.url} ${(chain.connection()?.protocol() ?: "")}\n")
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

        requestLog.append("--> END ${copyRequest.method}\n")

        Log.d(tag, requestLog.toString())

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            log("\u007C--> 请求失败: $e")
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        log(" ----------------------> 开始响应 <----------------------")
        val builder = response.newBuilder()
        val cloneResponse = builder.build()

        // 打印基础信息
        if (logBasic) {
            log("\u007C " + cloneResponse.code
                    + " " + cloneResponse.message
                    + " " + cloneResponse.request.url
                    + " (" + tookMs + "ms)")
        }
        // 打印Header信息
        if (logHeaders) {
            log("\u007C Headers:")
            val headers = cloneResponse.headers
            var i = 0
            val count = headers.size
            while (i < count) {
                log("\u007C     " + headers.name(i) + ": " + headers.value(i))
                i++
            }
        }
        // 打印Body信息
        if (logBody && cloneResponse.promisesBody()) {
            log("\u007C Body:")
            var responseBody = cloneResponse.body
            if (responseBody != null && isPlaintext(responseBody.contentType())) {
                val body = responseBody.string()
                log("\u007C     $body")
                log(" ----------------------> 结束响应 <----------------------")
                responseBody = body.toResponseBody(responseBody.contentType())
                return response.newBuilder().body(responseBody).build()
            } else {
                log("\u007C     body: maybe [file part] , too large too print , ignored!")
            }
        }
        log(" ----------------------> 结束响应 <----------------------")

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

    /**
     * 日志打印
     *
     * @param message
     */
    private fun log(message: String) {
//        Log.println(Log.DEBUG, tag, message)
    }
}