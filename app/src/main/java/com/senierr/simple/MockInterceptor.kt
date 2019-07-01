package com.senierr.simple

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * 模拟请求拦截器
 *
 * @author zhouchunjie
 * @date 2019/4/25 14:30
 */
class MockInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response: Response
        val request = chain.request()
        // 拦截指定地址
        if (request.url.toString().startsWith("https://api.test.cn", true)) {
            val responseBuilder = Response.Builder()
                    .code(200)
                    .message("ok")
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .addHeader("content-type", "application/json")
            val responseString = "{\"sys_time\":1501472461131,\"code\":0,\"msg\":\"ok\"}"
            responseBuilder.body(responseString.toResponseBody("application/json".toMediaTypeOrNull()))
            response = responseBuilder.build()
        } else {
            response = chain.proceed(request)
        }
        return response
    }
}