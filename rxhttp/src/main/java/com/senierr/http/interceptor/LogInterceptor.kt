package com.senierr.http.interceptor

import android.util.Log
import com.senierr.http.RxHttp
import com.senierr.http.util.Utils
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
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
        val requestBody = copyRequest.body()

        val requestLog = StringBuilder()
        requestLog.append(" \n┌─────────────────────────────────────────────────────────────────────────────────────\n")
        // 打印基础信息
        if (logBasic) {
            requestLog.append("├ ${copyRequest.method()} ${copyRequest.url()} ${(chain.connection()?.protocol() ?: "")}\n")
        }
        // 打印Header信息
        if (logHeaders) {
            val headers = copyRequest.headers()
            if (headers.size() > 0) {
                requestLog.append("├ Headers:\n")
            }
            for (i in 0 until headers.size()) {
                requestLog.append("│\t${headers.name(i)}: ${headers.value(i)}\n")
            }
        }
        // 打印Body信息
        if (logBody && requestBody != null) {
            requestLog.append("├ Body: (${requestBody.contentLength()}-byte)\n")
            if (Utils.isPlaintext(requestBody.contentType())) {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                var charset: Charset? = Utils.UTF8
                val contentType = requestBody.contentType()
                if (contentType != null) {
                    charset = contentType.charset(Utils.UTF8)
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
            responseLog.append("├ ${copyRequest.method()} ${copyRequest.url()} ${(chain.connection()?.protocol() ?: "")}\n")
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
            responseLog.append("├ ${cloneResponse.code()} ${cloneResponse.message()} ${cloneResponse.request().url()} (${tookMs}ms)\n")
        }
        // 打印Header信息
        if (logHeaders) {
            val headers = cloneResponse.headers()
            if (headers.size() > 0) {
                responseLog.append("├ Headers:\n")
            }
            for (i in 0 until headers.size()) {
                responseLog.append("│\t${headers.name(i)}: ${headers.value(i)}\n")
            }
        }
        // 打印Body信息
        if (logBody && Utils.hasBody(cloneResponse)) {
            var responseBody = cloneResponse.body()
            if (responseBody != null) {
                val contentLength = responseBody.contentLength()
                val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
                responseLog.append("├ Body: ($bodySize)\n")
                if (Utils.isPlaintext(responseBody.contentType())) {
                    val body = responseBody.string()
                    responseLog.append("│\t$body\n")
                    responseBody = ResponseBody.create(responseBody.contentType(), body)
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
}