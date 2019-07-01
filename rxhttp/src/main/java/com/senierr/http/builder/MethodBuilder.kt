package com.senierr.http.builder

/**
 * Http请求方法构建器
 *
 * @author zhouchunjie
 * @date 2018/8/29
 */
class MethodBuilder(private val value: String) : Builder<String> {

    companion object {
        const val GET = "GET"
        const val POST = "POST"
        const val PUT = "PUT"
        const val DELETE = "DELETE"
        const val HEAD = "HEAD"
        const val PATCH = "PATCH"
        const val OPTIONS = "OPTIONS"
        const val TRACE = "TRACE"
    }

    override fun build(): String {
        return value
    }
}