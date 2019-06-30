package com.senierr.http.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.internal.http.HttpHeaders
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

        /**
         * 判断body是否是文本内容
         */
        private fun isPlaintext(mediaType: MediaType?): Boolean {
            if (mediaType == null) return false
            if (mediaType.type() != null && mediaType.type() == "text") {
                return true
            }
            var subtype: String? = mediaType.subtype()
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

        log(" ----------------------> 开始请求 <----------------------")
        val copyRequest = request.newBuilder().build()
        val requestBody = copyRequest.body()
        val hasRequestBody = requestBody != null

        // 打印基础信息
        if (logBasic) {
            val connection = chain.connection()
            val requestStartMessage = ("\u007C " + copyRequest.method()
                    + " " + copyRequest.url()
                    + " " + if (connection != null) connection.protocol() else "")
            log(requestStartMessage)
        }
        // 打印Header信息
        if (logHeaders) {
            log("\u007C Headers:")
            val headers = copyRequest.headers()
            var i = 0
            val count = headers.size()
            while (i < count) {
                log("\u007C     " + headers.name(i) + ": " + headers.value(i))
                i++
            }
        }
        // 打印Body信息
        if (logBody && hasRequestBody) {
            log("\u007C Body:")
            if (isPlaintext(requestBody!!.contentType())) {
                val buffer = Buffer()
                requestBody.writeTo(buffer)

                var charset: Charset? = UTF8
                val contentType = requestBody.contentType()
                if (contentType != null) {
                    charset = contentType.charset(UTF8)
                }
                if (charset != null) {
                    log("\u007C     " + buffer.readString(charset))
                }
            } else {
                log("\u007C     Body maybe [file part] , too large too print , ignored!")
            }
        }

        log(" ----------------------> 结束请求 <----------------------")
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
            log("\u007C " + cloneResponse.code()
                    + " " + cloneResponse.message()
                    + " " + cloneResponse.request().url()
                    + " (" + tookMs + "ms)")
        }
        // 打印Header信息
        if (logHeaders) {
            log("\u007C Headers:")
            val headers = cloneResponse.headers()
            var i = 0
            val count = headers.size()
            while (i < count) {
                log("\u007C     " + headers.name(i) + ": " + headers.value(i))
                i++
            }
        }
        // 打印Body信息
        if (logBody && HttpHeaders.hasBody(cloneResponse)) {
            log("\u007C Body:")
            var responseBody = cloneResponse.body()
            if (responseBody != null && isPlaintext(responseBody.contentType())) {
                val body = responseBody.string()
                log("\u007C     $body")
                log(" ----------------------> 结束响应 <----------------------")
                responseBody = ResponseBody.create(responseBody.contentType(), body)
                return response.newBuilder().body(responseBody).build()
            } else {
                log("\u007C     body: maybe [file part] , too large too print , ignored!")
            }
        }
        log(" ----------------------> 结束响应 <----------------------")

        return response
    }

    /**
     * 日志打印
     *
     * @param message
     */
    private fun log(message: String) {
        Log.println(Log.DEBUG, tag, message)
    }
}