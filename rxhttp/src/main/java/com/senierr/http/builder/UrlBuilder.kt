package com.senierr.http.builder

import android.text.TextUtils

/**
 * Http请求行构建器
 *
 * @author zhouchunjie
 * @date 2018/8/29
 */
class UrlBuilder(private val url: String) : Builder<String> {

    private var baseUrl: String? = null
    private val baseUrlParams = LinkedHashMap<String, String>()
    private var ignoreBaseUrl = false
    private var ignoreBaseUrlParams = false

    private val urlParams: LinkedHashMap<String, String> = LinkedHashMap()

    fun baseUrl(baseUrl: String?) {
        this.baseUrl = baseUrl
    }

    fun addBaseUrlParam(key: String, value: String) {
        baseUrlParams[key] = value
    }

    fun addBaseUrlParams(params: LinkedHashMap<String, String>) {
        baseUrlParams.putAll(params)
    }

    fun ignoreBaseUrl() {
        ignoreBaseUrl = true
    }

    fun ignoreBaseUrlParams() {
        ignoreBaseUrlParams = true
    }

    fun addUrlParam(key: String, value: String) {
        urlParams[key] = value
    }

    fun addUrlParams(params: LinkedHashMap<String, String>) {
        urlParams.putAll(params)
    }

    override fun build(): String {
        // 若设置了基础请求地址，且未忽略，拼接基础请求地址
        var actualUrl = url
        if (!TextUtils.isEmpty(baseUrl) && !ignoreBaseUrl) {
            actualUrl = baseUrl + url
        }

        // 若设置了基础参数，且未忽略，添加基础参数（注意添加顺序）
        val actualUrlParams = LinkedHashMap<String, String>()
        if (baseUrlParams.isNotEmpty() && !ignoreBaseUrlParams) {
            actualUrlParams.putAll(baseUrlParams)
        }
        actualUrlParams.putAll(urlParams)

        // 生成URL
        if (actualUrlParams.isNotEmpty()) {
            val strParams = StringBuilder()
            if (actualUrl.contains("?")) {
                if (actualUrl.indexOf("?") != actualUrl.lastIndex) {
                    strParams.append("&")
                }
            } else {
                strParams.append("?")
            }

            actualUrlParams.keys.forEachIndexed { index, key ->
                if (index > 0) {
                    strParams.append("&")
                }
                strParams.append(key).append("=").append(actualUrlParams[key])
            }

            strParams.insert(0, actualUrl)
            actualUrl = strParams.toString()
        }
        return actualUrl
    }
}