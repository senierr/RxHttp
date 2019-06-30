package com.senierr.http.converter

import okhttp3.Response

/**
 * 默认转换器
 *
 * @author zhouchunjie
 * @date 2018/8/28
 */
class DefaultConverter : Converter<Response> {

    override fun convertResponse(response: Response): Response {
        return response
    }
}
