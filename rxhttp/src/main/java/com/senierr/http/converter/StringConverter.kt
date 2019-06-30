package com.senierr.http.converter

import okhttp3.Response
import okhttp3.internal.Util
import java.io.IOException
import java.nio.charset.Charset

/**
 * 字符串转换器
 *
 * @author zhouchunjie
 * @date 2018/8/28
 */
class StringConverter(private val charset: Charset? = null) : Converter<String> {

    override fun convertResponse(response: Response): String {
        val responseBody = response.body() ?: throw IOException("ResponseBody is null!")

        return if (charset != null) {
            val source = responseBody.source()
            try {
                source.readString(charset)
            } finally {
                Util.closeQuietly(source)
            }
        } else {
            responseBody.string()
        }
    }
}
