package com.senierr.http.util

import okhttp3.MediaType
import okhttp3.Response
import java.io.Closeable
import java.net.HttpURLConnection.HTTP_NOT_MODIFIED
import java.net.HttpURLConnection.HTTP_NO_CONTENT
import java.net.URLConnection
import java.nio.charset.Charset

/**
 * 工具类
 *
 * @author zhouchunjie
 * @date 2019/7/10
 */
object Utils {

    val UTF8 = Charset.forName("UTF-8")

    /**
     * 是否有返回体
     */
    fun hasBody(response: Response): Boolean {
        if (response.request().method() == "HEAD") {
            return false
        }

        val responseCode = response.code()
        if ((responseCode < 100 || responseCode >= 200)
                && responseCode != HTTP_NO_CONTENT
                && responseCode != HTTP_NOT_MODIFIED) {
            return true
        }

        var contentLength = -1L
        try {
            contentLength = response.headers().get("Content-Length")?.toLong() ?: -1L
        } catch (e: Exception) {
        }

        return contentLength != -1L || "chunked".equals(response.header("Transfer-Encoding"), ignoreCase = true)
    }

    /**
     * 获取可能的内容格式
     */
    fun guessMimeType(path: String, defaultContentType: String): MediaType? {
        val result = path.replace("#", "")   //解决文件名中含有#号异常的问题
        val fileNameMap = URLConnection.getFileNameMap()
        var contentType = fileNameMap.getContentTypeFor(result)
        if (contentType == null) {
            contentType = defaultContentType
        }
        return MediaType.parse(contentType)
    }

    /**
     * 判断body是否是文本内容
     */
    fun isPlaintext(mediaType: MediaType?): Boolean {
        if (mediaType == null) return false
        if (mediaType.type() == "text") {
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

    /**
     * 安静的关闭流
     */
    fun closeQuietly(vararg closeable: Closeable?) {
        closeable.forEach {
            try {
                it?.close()
            } catch (rethrown: RuntimeException) {
                throw rethrown
            } catch (ignored: Exception) {
            }
        }
    }
}