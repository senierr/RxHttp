package com.senierr.http.builder

import okhttp3.Headers

/**
 * Http请求头构建器
 *
 * @author zhouchunjie
 * @date 2018/8/29
 */
class HeaderBuilder : Builder<Headers> {

    private val baseHeaders = LinkedHashMap<String, String>()
    private var ignoreBaseHeaders = false

    private val headers = LinkedHashMap<String, String>()

    fun addBaseHeader(key: String, value: String) {
        baseHeaders[key] = value
    }

    fun addBaseHeaders(headers: LinkedHashMap<String, String>) {
        baseHeaders.putAll(headers)
    }

    fun ignoreBaseHeaders() {
        ignoreBaseHeaders = true
    }

    fun addHeader(key: String, value: String) {
        headers[key] = value
    }

    fun addHeaders(headers: LinkedHashMap<String, String>) {
        this.headers.putAll(headers)
    }

    override fun build(): Headers {
        // 若设置了基础头，且未忽略，添加基础头（注意添加顺序）
        val actualHeaders = LinkedHashMap<String, String>()
        if (baseHeaders.isNotEmpty() && !ignoreBaseHeaders) {
            actualHeaders.putAll(baseHeaders)
        }
        actualHeaders.putAll(headers)

        val builder = Headers.Builder()
        for (key in actualHeaders.keys) {
            val value = actualHeaders[key]
            value?.let { builder.add(key, it) }
        }
        return builder.build()
    }
}